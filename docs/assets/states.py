import re

def patch(lines):
    return [
        re.sub(">yes(.*)<", ":material-check:\\1 { data-sort='1\\\\\\1' }",
        re.sub(">kinda(.*)<", ":material-plus-minus-variant:\\1 { data-sort='2\\\\\\1' }",
        re.sub(">no(.*)<", ":material-close:\\1 { data-sort='3\\\\\\1' }", i)))
    for i in lines]