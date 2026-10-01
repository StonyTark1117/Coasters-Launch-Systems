"""Regenerate original equipment JSON geometry and standard crafting data."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD = "coasterslaunchsystems"

def save(path, value):
    dest = ROOT / path
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(value, indent=2) + "\n")

def box(start, end, texture):
    return {"from": start, "to": end, "faces": {d: {"texture": texture} for d in ("up", "down", "north", "south", "east", "west")}}

modules = {"tire_drive": ("Tire Drive Module", "minecraft:block/black_concrete"),
           "fin_brake": ("Fin Brake Module", "minecraft:block/red_terracotta"),
           "eddy_brake": ("Passive Magnetic Brake Module", "minecraft:block/blue_terracotta")}
lang = {f"item.{MOD}.span_tool": "Launch Span Tool"}
for name, (label, panel) in modules.items():
    lang[f"block.{MOD}.{name}"] = label
    elements = [box([1, 0, 1], [15, 3, 15], "#steel"),
                box([2, 3, 2], [14, 11, 14], "#steel"),
                box([4, 6, 0], [12, 10, 2], "#panel"),
                box([5, 11, 5], [11, 13, 11], "#panel")]
    if name == "tire_drive":
        for y in (1, 12):
            elements.append(box([3, y, 3], [13, y + 3, 13], "#panel"))
            wheel = box([3, y, 3], [13, y + 3, 13], "#panel")
            wheel["rotation"] = {"origin": [8, y + 1.5, 8], "axis": "y", "angle": 45}
            elements.append(wheel)
    else:
        for x in (3, 10):
            elements.append(box([x, 11, 3], [x + 3, 16, 13], "#panel"))
    save(f"assets/{MOD}/models/block/{name}.json", {"parent": "minecraft:block/block", "textures": {
        "steel": "minecraft:block/iron_block", "panel": panel, "particle": "minecraft:block/iron_block"}, "elements": elements})
    save(f"assets/{MOD}/blockstates/{name}.json", {"variants": {
        "axis=y": {"model": f"{MOD}:block/{name}"},
        "axis=z": {"model": f"{MOD}:block/{name}", "x": 90},
        "axis=x": {"model": f"{MOD}:block/{name}", "x": 90, "y": 90}}})
    save(f"assets/{MOD}/models/item/{name}.json", {"parent": f"{MOD}:block/{name}"})
    save(f"data/{MOD}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
        "entries": [{"type": "minecraft:item", "name": f"{MOD}:{name}"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    part = {"item": "create:belt_connector"} if name == "tire_drive" else {"item": "create:iron_sheet"}
    save(f"data/{MOD}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["IPI", "SCS", "IRI"],
        "key": {"I": {"tag": "c:ingots/iron"}, "P": part, "S": {"item": "create:shaft"},
                "C": {"item": "create:andesite_casing"}, "R": {"item": "minecraft:redstone"}},
        "result": {"id": f"{MOD}:{name}", "count": 1}})
save(f"assets/{MOD}/lang/en_us.json", lang)
save(f"assets/{MOD}/models/item/span_tool.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": "minecraft:item/iron_nugget"}})
save(f"data/{MOD}/recipe/span_tool.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": [" I", "S "],
    "key": {"I": {"item": "minecraft:iron_nugget"}, "S": {"item": "minecraft:stick"}}, "result": {"id": f"{MOD}:span_tool"}})
save("data/minecraft/tags/block/mineable/pickaxe.json", {"replace": False, "values": [f"{MOD}:{n}" for n in modules]})
lang[f"block.{MOD}.tire_surface"] = "Chassis Tire Drive Strip"
save(f"assets/{MOD}/lang/en_us.json", lang)
save(f"assets/{MOD}/models/block/tire_surface.json", {"parent": "minecraft:block/block", "textures": {
    "rubber": "minecraft:block/black_concrete", "particle": "minecraft:block/black_concrete"},
    "elements": [box([1, 14, 1], [15, 16, 15], "#rubber")]})
save(f"assets/{MOD}/blockstates/tire_surface.json", {"variants": {"": {"model": f"{MOD}:block/tire_surface"}}})
save(f"assets/{MOD}/models/item/tire_surface.json", {"parent": f"{MOD}:block/tire_surface"})
save(f"data/{MOD}/loot_table/blocks/tire_surface.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
    "entries": [{"type": "minecraft:item", "name": f"{MOD}:tire_surface"}],
    "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
save(f"data/{MOD}/recipe/tire_surface.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "pattern": ["BIB"],
    "key": {"B": {"item": "create:belt_connector"}, "I": {"item": "create:iron_sheet"}},
    "result": {"id": f"{MOD}:tire_surface", "count": 2}})
save("data/minecraft/tags/block/mineable/pickaxe.json", {"replace": False, "values": [f"{MOD}:{n}" for n in [*modules, "tire_surface"]]})
