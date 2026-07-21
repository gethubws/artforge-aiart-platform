"""Generate the Emerald Fable Forest demo resource pack.

Required environment variable:
  AIART_IMAGE_API_KEY

Optional environment variables:
  AIART_IMAGE_API_BASE (default: https://llm-all.pro/v1)
  AIART_IMAGE_MODEL (default: gpt-image-2)
  AIART_IMAGE_CONCURRENCY (default: 3)
"""

from __future__ import annotations

import base64
import json
import os
import time
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from io import BytesIO
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "frontend" / "public" / "images" / "style-packs" / "emerald-fable"
CACHE_DIR = ROOT / "tmp" / "style-pack-generation" / "emerald-fable"
API_BASE = os.environ.get("AIART_IMAGE_API_BASE", "https://llm-all.pro/v1").rstrip("/")
API_KEY = os.environ.get("AIART_IMAGE_API_KEY", "")
MODEL = os.environ.get("AIART_IMAGE_MODEL", "gpt-image-2")
CONCURRENCY = max(1, min(4, int(os.environ.get("AIART_IMAGE_CONCURRENCY", "3"))))

STYLE = (
    "Emerald Fable Forest game art direction: polished stylized hand-painted 3D fantasy, "
    "jade green and moss, pale gold trim, restrained coral accents, soft diffuse daylight, "
    "rounded readable silhouettes, tactile painted surfaces, coherent premium indie game asset pack. "
)

SHEETS = [
    {
        "key": "vegetation",
        "category": "VEGETATION",
        "items": ["萤光古橡树", "翡翠蕨丛", "星点蘑菇簇", "卷叶草坪块"],
        "objects": "an ancient luminous oak tree; a dense emerald fern cluster; a cluster of star-speckled mushrooms; a curled-leaf grass tile",
    },
    {
        "key": "architecture",
        "category": "ARCHITECTURE",
        "items": ["林地炼金小屋", "金叶石门", "月泉祭坛", "苔桥"],
        "objects": "a tiny woodland alchemist cottage; a pale-gold leaf stone gate; a moonwell forest shrine; a moss-covered arched bridge",
    },
    {
        "key": "props",
        "category": "PROPS",
        "items": ["萤石提灯", "藤蔓宝箱", "森林药剂瓶", "蘑菇路牌"],
        "objects": "a glowing gemstone lantern; a vine-wrapped treasure chest; a forest potion bottle; a mushroom-shaped wooden signpost",
    },
    {
        "key": "characters",
        "category": "CHARACTERS",
        "items": ["翡翠游侠", "月露法师", "林地商人", "金叶守卫"],
        "objects": "a full-body emerald forest ranger; a full-body moon-dew mage; a full-body friendly woodland merchant; a full-body pale-gold leaf guardian",
    },
    {
        "key": "creatures",
        "category": "CREATURES",
        "items": ["苔角鹿", "珊瑚尾狐", "月羽猫头鹰", "露珠史莱姆"],
        "objects": "a moss-antler deer; a coral-tailed fox; a moon-feather owl; a translucent dew-drop slime",
    },
    {
        "key": "terrain",
        "category": "TERRAIN",
        "items": ["苔石地砖", "翡翠悬崖块", "林地木板路", "月泉水面块"],
        "objects": "a seamless mossy stone ground tile; a modular emerald cliff chunk; a modular woodland plank path; a seamless moonwell water tile",
    },
    {
        "key": "ui",
        "category": "UI",
        "items": ["生命叶图标", "法力露珠图标", "任务卷轴图标", "背包按钮图标"],
        "objects": "a leaf-shaped health icon; a dew-drop mana icon; a quest scroll icon; a backpack menu icon",
    },
    {
        "key": "effects",
        "category": "BACKGROUNDS",
        "items": ["萤火粒子效果", "月泉法阵", "叶片冲击效果", "森林传送门"],
        "objects": "a firefly particle effect cluster; a moonwell magic circle; a leaf burst impact effect; an emerald forest portal",
    },
]


def request_image(prompt: str) -> bytes:
    payload = json.dumps({
        "model": MODEL,
        "prompt": prompt,
        "size": "1024x1024",
        "n": 1,
    }).encode("utf-8")
    request = urllib.request.Request(
        f"{API_BASE}/images/generations",
        data=payload,
        headers={
            "Authorization": f"Bearer {API_KEY}",
            "Content-Type": "application/json",
        },
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=360) as response:
        body = json.loads(response.read().decode("utf-8"))
    item = body["data"][0]
    if item.get("b64_json"):
        return base64.b64decode(item["b64_json"])
    if item.get("url"):
        with urllib.request.urlopen(item["url"], timeout=360) as response:
            return response.read()
    raise RuntimeError("Image API returned neither b64_json nor url")


def generate_with_retry(key: str, prompt: str) -> tuple[str, bytes]:
    last_error = None
    for attempt in range(1, 5):
        try:
            return key, request_image(prompt)
        except Exception as error:  # noqa: BLE001
            last_error = error
            if attempt < 4:
                time.sleep(10 * attempt)
    raise RuntimeError(f"Failed to generate {key}: {last_error}")


def sheet_prompt(definition: dict) -> str:
    return (
        STYLE
        + "Create one square production asset sheet with a strict 2 by 2 grid of four equal cells. "
        + "Each cell contains exactly one isolated asset centered on a clean very light mint studio background, "
        + "consistent camera angle and scale, generous padding, no overlap across cells. "
        + f"Top-left, top-right, bottom-left, bottom-right respectively: {definition['objects']}. "
        + "No labels, no letters, no numbers, no watermark, no border decorations, no extra objects."
    )


def cover_prompt() -> str:
    return (
        STYLE
        + "Create a wide-looking key art scene composed inside a square image for a game asset pack cover. "
        + "Show the ancient luminous oak, woodland cottage, moonwell shrine, moss bridge, ranger, fox, lantern, "
        + "ferns and mushrooms together in one readable forest clearing. Bright daylight, visible asset details, "
        + "no text, no logo, no watermark, no dark vignette."
    )


def save_sheet(definition: dict, data: bytes) -> list[dict]:
    image = Image.open(BytesIO(data)).convert("RGBA")
    width, height = image.size
    boxes = [
        (0, 0, width // 2, height // 2),
        (width // 2, 0, width, height // 2),
        (0, height // 2, width // 2, height),
        (width // 2, height // 2, width, height),
    ]
    assets = []
    object_prompts = definition["objects"].split("; ")
    for index, (name, box, object_prompt) in enumerate(zip(definition["items"], boxes, object_prompts), start=1):
        filename = f"{definition['key']}-{index:02d}.png"
        cropped = image.crop(box)
        cropped.save(OUTPUT_DIR / filename)
        assets.append({
            "name": name,
            "categoryKey": definition["category"],
            "filename": filename,
            "width": cropped.width,
            "height": cropped.height,
            "fileFormat": "PNG",
            "backgroundMode": "SOLID" if definition["category"] not in {"UI", "BACKGROUNDS"} else "SCENE",
            "promptText": STYLE + object_prompt,
        })
    return assets


def cache_path(key: str) -> Path:
    return CACHE_DIR / f"{key}.png"


def main() -> None:
    if not API_KEY:
        raise SystemExit("AIART_IMAGE_API_KEY is required")
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    jobs = {definition["key"]: sheet_prompt(definition) for definition in SHEETS}
    jobs["cover"] = cover_prompt()
    generated: dict[str, bytes] = {
        key: cache_path(key).read_bytes()
        for key in jobs
        if cache_path(key).exists()
    }
    pending_jobs = {key: prompt for key, prompt in jobs.items() if key not in generated}
    errors = []
    with ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
        futures = [executor.submit(generate_with_retry, key, prompt) for key, prompt in pending_jobs.items()]
        for future in as_completed(futures):
            try:
                key, data = future.result()
                generated[key] = data
                cache_path(key).write_bytes(data)
                print(f"generated {key}", flush=True)
            except Exception as error:  # noqa: BLE001
                errors.append(str(error))
                print(str(error), flush=True)

    if errors:
        raise RuntimeError("Some images failed; rerun to continue from cache:\n" + "\n".join(errors))

    cover = Image.open(BytesIO(generated["cover"])).convert("RGB")
    cover.save(OUTPUT_DIR / "cover.png")
    assets = []
    for definition in SHEETS:
        assets.extend(save_sheet(definition, generated[definition["key"]]))
    manifest = {
        "name": "翡翠童话森林",
        "slug": "emerald-fable",
        "cover": "cover.png",
        "assets": assets,
    }
    (OUTPUT_DIR / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"output": str(OUTPUT_DIR), "assetCount": len(assets)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
