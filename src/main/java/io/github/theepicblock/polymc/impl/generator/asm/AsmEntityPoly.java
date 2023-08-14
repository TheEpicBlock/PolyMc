package io.github.theepicblock.polymc.impl.generator.asm;

import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.item.CustomModelDataManager;
import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.json.*;
import io.github.theepicblock.polymc.api.wizard.PacketConsumer;
import io.github.theepicblock.polymc.api.wizard.VInteraction;
import io.github.theepicblock.polymc.api.wizard.Wizard;
import io.github.theepicblock.polymc.api.wizard.WizardInfo;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import io.github.theepicblock.polymc.impl.poly.entity.EntityWizard;
import io.github.theepicblock.polymc.impl.resource.ResourceConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AsmEntityPoly<T extends Entity> implements EntityPoly<T> {
    private final ExecutionGraphNode graph;
    private final EntityType<T> sourceType;
    private final Map<ExecutionGraphNode.RenderCall, ItemStack> cmdItems;

    public AsmEntityPoly(ExecutionGraphNode graph, EntityType<T> sourceType, CustomModelDataManager cmd) {
        this.graph = graph;
        this.sourceType = sourceType;
        this.cmdItems = new HashMap<>();

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
        copy(moddedResources, pack, texture);
        this.cmdItems.forEach((call, item) -> {
            var generatedModelLocation = new Identifier("poly-asm", entityId.getPath()+"-"+call.hashCode());
            var cube = call.cuboid().extractAs(Cuboid.class);

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
                                            Arrays.stream(side.vertices).map(p -> p.u).min(Float::compareTo).orElse(0F),
                                            Arrays.stream(side.vertices).map(p -> p.v).min(Float::compareTo).orElse(0F),
                                            Arrays.stream(side.vertices).map(p -> p.u).max(Float::compareTo).orElse(16F),
                                            Arrays.stream(side.vertices).map(p -> p.v).max(Float::compareTo).orElse(16F)},
                                    "tex1",
                                    null,
                                    0,
                                    0
                            )
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
        private final ExecutionGraphNode graph;
        private final VInteraction mainEntity;
        private final VirtualMachine hehe = new VirtualMachine(new ClientClassLoader(), new VirtualMachine.VmConfig() {});

        public AsmEntityWizard(WizardInfo info, T entity, AsmEntityPoly<T> parent) {
            super(info, entity);
            this.graph = parent.graph;

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
