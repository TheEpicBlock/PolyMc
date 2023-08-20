package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.PolyMc;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.*;
import io.github.theepicblock.polymc.api.wizard.*;
import io.github.theepicblock.polymc.impl.generator.asm.stack.KnownObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.MockedObject;
import io.github.theepicblock.polymc.impl.generator.asm.stack.StackEntry;
import io.github.theepicblock.polymc.impl.generator.asm.stack.UnknownValue;
import io.github.theepicblock.polymc.impl.generator.asm.stack.ops.StaticFieldValue;
import io.github.theepicblock.polymc.impl.misc.InternalEntityHelpers;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.entity.EntityWizard;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.*;
import java.util.stream.Collectors;

public class AsmEntityPoly<T extends Entity> implements EntityPoly<T> {
    private final ExecutionGraphNode graph;
    private final EntityType<T> sourceType;
    private final Map<ExecutionGraphNode.RenderCall, ItemStack> cmdItems;
    private final Class<?> entityClass;

    public AsmEntityPoly(ExecutionGraphNode graph, EntityType<T> sourceType, CustomModelDataManager cmd) {
        this.graph = graph;
        this.sourceType = sourceType;
        this.cmdItems = new HashMap<>();
        this.entityClass = InternalEntityHelpers.getEntityClass(sourceType);

        var calls = this.graph.getUniqueCalls();
        calls.forEach(call -> {
            var itemInfo = cmd.requestCMD(CustomModelDataManager.GENERATED_MODEL_ITEMS);
            var stack = new ItemStack(itemInfo.getLeft());
            stack.getOrCreateNbt().putInt("CustomModelData", itemInfo.getRight());

            cmdItems.put(call, stack);
        });
    }

    @Override
    public void addToResourcePack(EntityType<?> entityType, ModdedResources moddedResources, PolyMcResourcePack pack, SimpleLogger logger) {
        var entityId = Registries.ENTITY_TYPE.getId(entityType);
        var texture = new Identifier("polymc-testmod", "textures/entity/asm_entity.png");
        var texWidth = 16;
        var texHeight = 16;
        copy(moddedResources, pack, texture);
        this.cmdItems.forEach((call, item) -> {
            var generatedModelLocation = new Identifier("poly-asm", entityId.getPath()+"-"+call.hashCode());
            var cube = call.cuboid().extractAs(Cuboid.class);

            cube.sides[0].direction.set(Direction.DOWN.getUnitVector());
            cube.sides[1].direction.set(Direction.UP.getUnitVector());
            cube.sides[2].direction.set(Direction.WEST.getUnitVector());
            cube.sides[3].direction.set(Direction.NORTH.getUnitVector());
            cube.sides[4].direction.set(Direction.EAST.getUnitVector());
            cube.sides[5].direction.set(Direction.SOUTH.getUnitVector());
            var faces = Arrays.stream(cube.sides)
                    .collect(Collectors.toMap(
                            side -> {
                                var directionVec = side.direction();
                                var direction = Direction.fromVector((int)directionVec.x, (int)directionVec.y, (int)directionVec.z);
                                assert direction != null;
                                return JDirection.fromMojang(direction);
                            },
                            side -> new JElementFace(
                                    new double[]{
                                            Arrays.stream(side.vertices).map(p -> p.u).min(Float::compareTo).orElse(0F)* texWidth % texWidth,
                                            Arrays.stream(side.vertices).map(p -> p.v).min(Float::compareTo).orElse(0F) * texHeight % texHeight,
                                            Arrays.stream(side.vertices).map(p -> p.u).max(Float::compareTo).orElse(16F)* texWidth % texWidth,
                                            Arrays.stream(side.vertices).map(p -> p.v).max(Float::compareTo).orElse(16F) * texHeight % texHeight},
                                    "tex1",
                                    null,
                                    0,
                                    0
                            ),
                            (side1, side2) -> side2
                    ));

            var model = JModel.create();
            model.getTextures().put("tex1", texture.toString());
            model.getElements().add(
                    new JElement(
                            new double[]{cube.minX, cube.minY, cube.minZ},
                            new double[]{cube.maxX, cube.maxY, cube.maxZ},
                            null,
                            true,
                            faces
                    )
            );

            copy(moddedResources, pack, texture);
            pack.setItemModel(generatedModelLocation.getNamespace(), generatedModelLocation.getPath(), model);

            // Add the model as an override
            var clientItem = item.getItem();
            var clientItemId = Registries.ITEM.getId(clientItem);
            assert item.getNbt() != null;
            var cmdId = item.getNbt().getInt("CustomModelData");

            var clientModel = pack.getOrDefaultVanillaItemModel(moddedResources, clientItemId.getNamespace(), clientItemId.getPath(), logger);
            clientModel.getOverrides().add(JModelOverride.ofCMD(cmdId, ResourceConstants.itemLocation(generatedModelLocation)));
        });
    }

    private void copy(ModdedResources from, PolyMcResourcePack to, Identifier id) {
        to.setAsset(id.getNamespace(), id.getPath(), from.getAsset(id.getNamespace(), id.getPath()));
    }

    @Override
    public Wizard createWizard(WizardInfo info, T entity) {
        return new AsmEntityWizard<>(info, entity, this);
    }

    public static class AsmEntityWizard<T extends Entity> extends EntityWizard<T> {
        private final AsmEntityPoly<T> poly;
        private final VirtualMachine hehe = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {
            @Override
            public @NotNull StackEntry loadStaticField(VirtualMachine.Context ctx, VirtualMachine.Clazz owner, String fieldName) throws MethodExecutor.VmException {
                var fromEnvironment = AsmUtils.tryGetStaticFieldFromEnvironment(ctx, owner.name(), fieldName);
                if (fromEnvironment != null) return fromEnvironment;
                return new StaticFieldValue(owner.name(), fieldName).simplify(hehe);
            }

            @Override
            public StackEntry onVmError(String method, boolean returnsVoid, MethodExecutor.VmException e) throws MethodExecutor.VmException {
                if (returnsVoid) return null;
                return new UnknownValue("Error executing " + method + ": " + e.createFancyErrorMessage());
            }

            @Override
            public boolean shouldSimplifyVmObjects() {
                return true;
            }

            @Override
            public void invoke(VirtualMachine.Context ctx, VirtualMachine.Clazz currentClass, MethodInsnNode inst, StackEntry[] arguments, VirtualMachine.@Nullable MethodRef meth) throws MethodExecutor.VmException {
                if (meth != null) {
                    // Try executing in the real vm
                    try {
                        if (AsmUtils.isAllConcrete(arguments)) {
                            var clazz = Class.forName(meth.className().replace("/", "."));

                            var argTypes = Type.getArgumentTypes(meth.desc());
                            var types = new Class<?>[argTypes.length];
                            var args = new Object[argTypes.length];
                            for (int i = 0; i < argTypes.length; i++) {
                                types[i] = Class.forName(argTypes[i].getClassName());
                            }
                            for (int i = 0; i < argTypes.length; i++) {
                                args[i] = arguments[i + inst.getOpcode() == Opcodes.INVOKESTATIC ? 0 : 1].extractAs(types[i]);
                            }

                            var jMeth = clazz.getDeclaredMethod(meth.name(), types);
                            ret(ctx, StackEntry.known(jMeth.invoke(inst.getOpcode() == Opcodes.INVOKESTATIC ? null : arguments[0].extractAs(clazz), args)));
                            return;
                        }
                    } catch (Throwable ignored) {}


                }

                VirtualMachine.VmConfig.super.invoke(ctx, currentClass, inst, arguments, meth);
            }
        });
        private final HashMap<ExecutionGraphNode.RenderCall,VItemDisplay> calls = new HashMap<>();
        private final VInteraction mainEntity;

        public AsmEntityWizard(WizardInfo info, T entity, AsmEntityPoly<T> parent) {
            super(info, entity);
            this.poly = parent;

            this.mainEntity = new VInteraction(entity.getUuid(), entity.getId());
        }

        @Override
        public void addPlayer(PacketConsumer player) {
            var source = this.getEntity();
            mainEntity.spawn(player, this.getPosition());
            mainEntity.setup(player, source.getWidth(), source.getHeight(), false);
        }

        @Override
        public void removePlayer(PacketConsumer player) {
            mainEntity.remove(player);
        }

        @Override
        public void onTick(PacketConsumer players) {
            try {
                var cache = new Reference2ReferenceOpenHashMap<StackEntry, StackEntry>();

                MockedObject.MOCKED_RESOLVERS.put("entity", StackEntry.known(this.getEntity()));
                MockedObject.MOCKED_RESOLVERS.put("hasLabel", StackEntry.known(true));
                MockedObject.MOCKED_RESOLVERS.put("yaw", StackEntry.known(this.getEntity().getYaw()));
                MockedObject.MOCKED_RESOLVERS.put("tickDelta", StackEntry.known(0F));
                MockedObject.MOCKED_RESOLVERS.put("light", StackEntry.known(0));

                var node = this.poly.graph;
                HashSet<ExecutionGraphNode.RenderCall> newCalls = new HashSet<>();
                while (true) {
                    if (node.getCalls() != null) {
                        newCalls.addAll(node.getCalls());
                    }

                    var cont = node.getContinuation();
                    if (cont == null) break;

                    var compA = cont.compA();
                    var compB = cont.compB();

                    compA = compA.copyTmp().simplify(hehe, cache);
                    if (compB != null) compB = compB.copyTmp().simplify(hehe, cache);

                    if (!compA.isConcrete() || (compB != null && !compB.isConcrete())) {
//                        PolyMc.LOGGER.warn("Error ticking entity "+this.getEntity().getType()+" non-concrete value");
                        break;
                    }

                    var isSucc = switch (cont.opcode()) {
                        case Opcodes.IF_ACMPEQ -> MethodExecutor.stackEntryIdentityEqual(compA, compB);
                        case Opcodes.IF_ACMPNE -> !MethodExecutor.stackEntryIdentityEqual(compA, compB);
                        case Opcodes.IFNULL -> compA instanceof KnownObject o && o.i() == null;
                        case Opcodes.IFNONNULL -> !(compA instanceof KnownObject o && o.i() == null);
                        case Opcodes.IF_ICMPEQ -> Objects.equals(compA.extractAs(int.class), compB.extractAs(int.class));
                        case Opcodes.IF_ICMPNE -> !Objects.equals(compA.extractAs(int.class), compB.extractAs(int.class));
                        case Opcodes.IF_ICMPLT -> compA.extractAs(int.class) < compB.extractAs(int.class);
                        case Opcodes.IF_ICMPLE -> compA.extractAs(int.class) <= compB.extractAs(int.class);
                        case Opcodes.IF_ICMPGT -> compA.extractAs(int.class) > compB.extractAs(int.class);
                        case Opcodes.IF_ICMPGE -> compA.extractAs(int.class) >= compB.extractAs(int.class);
                        case Opcodes.IFEQ -> compA.extractAs(int.class) == 0;
                        case Opcodes.IFNE -> compA.extractAs(int.class) != 0;
                        case Opcodes.IFLT -> compA.extractAs(int.class) < 0;
                        case Opcodes.IFLE -> compA.extractAs(int.class) <= 0;
                        case Opcodes.IFGT -> compA.extractAs(int.class) > 0;
                        case Opcodes.IFGE -> compA.extractAs(int.class) >= 0;
                        default -> throw new NotImplementedException("Can't compare "+cont.opcode());
                    };

                    if (isSucc) {
                        node = cont.continuationIfTrue();
                    } else {
                        node = cont.continuationIfFalse();
                    }
                }

                var iterator = calls.entrySet().iterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    var key = entry.getKey();
                    var value = entry.getValue();
                    if (!newCalls.contains(key)) {
                        value.remove(players);
                        iterator.remove();
                    } else {
                        value.move(players, this.getPosition(), 0, 0, false);
                        try {
                            var matrixEntry = key.matrix().copyTmp().simplify(hehe, cache);
                            var matrix = matrixEntry.extractAs(Matrix4f.class);

                            // To counter-act the transformations that the item and displayentity renderers do
//                            matrix.translate(0.5f, 0f, 0.5f); // TODO, investigate why the model is rendered 0.5 too high
//                            matrix.rotate(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));

                            value.setupTransforms(players, new AffineTransformation(matrix));
                        } catch (MethodExecutor.VmException e) {
                            PolyMc.LOGGER.warn("exception calculating matrix for entity "+this.getEntity().getType()+" "+e.createFancyErrorMessage());
                        }
                    }
                }

                newCalls.forEach(call -> {
                    if (!calls.containsKey(call)) {
                        var display = new VItemDisplay();
                        calls.put(call, display);
                        display.spawn(players, this.getPosition());
                        display.sendItem(players, poly.cmdItems.get(call));
                    }
                });
            } catch (MethodExecutor.VmException e) {
                PolyMc.LOGGER.warn("exception ticking entity "+this.getEntity().getType()+" "+e.createFancyErrorMessage());
            }
            MockedObject.MOCKED_RESOLVERS.clear();

            super.onTick(players);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    // TODO mappings
    private record Cuboid(
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            Quad[] sides) {

    }
    private record Quad(Vertex[] vertices, Vector3f direction) {

    }

    private record Vertex(float u, float v) {

    }
}
