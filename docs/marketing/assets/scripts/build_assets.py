#!/usr/bin/env python3
"""Generate Google Play marketing assets from editable SVG sources.

Usage:
    python build_assets.py              # generate SVG sources + PNG previews
    python build_assets.py --svg-only   # regenerate SVG sources only
    python build_assets.py --png-only   # render PNG from existing SVG sources
"""
from __future__ import annotations
import argparse
import json
import sys
import textwrap
from pathlib import Path
from xml.sax.saxutils import escape

sys.path.insert(0, str(Path(__file__).resolve().parent))

try:
    import cairosvg
except ImportError:
    cairosvg = None

from render_pillow import FEATURE_DECOR_CIRCLES, FEATURE_GRAPHIC, ensure_icon_png, render_all_from_config

ROOT: Path = Path(__file__).resolve().parent.parent
SOURCES: Path = ROOT / "sources"
SCREENSHOTS: Path = ROOT / "screenshots"
OUTPUT: Path = ROOT / "output"
CONFIG_PATH: Path = ROOT / "config.json"

PHONE = {
    "x": 90,
    "y": 280,
    "width": 900,
    "height": 1580,
    "radius": 48,
    "bezel": 16,
    "screen_radius": 36,
}


def load_config() -> dict:
    with CONFIG_PATH.open(encoding="utf-8") as file:
        return json.load(file)


def enabled_screenshots(config: dict) -> list[dict]:
    return [item for item in config["screenshots"] if not item.get("disabled")]


def brand_name(brand: dict, locale: str) -> str:
    key: str = f"name{locale.capitalize()}"
    return brand.get(key, brand.get("nameEn", "Numbers"))


def feature_title(feature_graphic: dict, locale: str) -> str:
    key: str = f"title{locale.capitalize()}"
    return feature_graphic.get(key, feature_graphic.get("titleEn", feature_graphic.get("title", "Numbers")))


def banner_tagline(locale: str) -> str:
    if locale == "ru":
        return "Бесплатно · Без рекламы · RU/EN"
    return "Free · No ads · RU/EN"


def remove_disabled_screenshot_assets(config: dict) -> None:
    for item in config["screenshots"]:
        if not item.get("disabled"):
            continue
        for locale in ("ru", "en"):
            (SOURCES / "screenshots" / locale / f"{item['id']}.svg").unlink(missing_ok=True)
            (OUTPUT / "screenshots" / locale / f"{item['id']}.png").unlink(missing_ok=True)


def wrap_caption(text: str, max_chars: int = 28) -> list[str]:
    if "\n" in text:
        manual_lines: list[str] = [line.strip() for line in text.split("\n") if line.strip()]
        return manual_lines if manual_lines else [text]
    lines: list[str] = textwrap.wrap(text, width=max_chars)
    if not lines:
        return [text]
    if len(lines) == 2 and len(lines[1].split()) == 1:
        words: list[str] = text.split()
        if len(words) >= 3:
            first_line: str = " ".join(words[:-2])
            second_line: str = " ".join(words[-2:])
            if len(first_line) <= max_chars + 8 and len(second_line) <= max_chars:
                return [first_line, second_line]
    return lines


def screenshot_href(screenshot_file: str) -> str:
    path: Path = SCREENSHOTS / screenshot_file
    if path.exists():
        return path.resolve().as_uri()
    return ""


def placeholder_svg(x: int, y: int, width: int, height: int, label: str) -> str:
    center_x: int = x + width // 2
    center_y: int = y + height // 2
    return f"""
    <rect x="{x}" y="{y}" width="{width}" height="{height}" rx="8"
          fill="#F1EDEC" stroke="#C4C7C7" stroke-width="3" stroke-dasharray="16 12"/>
    <text x="{center_x}" y="{center_y - 20}" text-anchor="middle"
          font-family="Segoe UI, Arial, sans-serif" font-size="36" fill="#747878">
      Вставьте скриншот
    </text>
    <text x="{center_x}" y="{center_y + 30}" text-anchor="middle"
          font-family="Segoe UI, Arial, sans-serif" font-size="28" fill="#747878">
      {escape(label)}
    </text>
    """


def phone_frame_svg(brand: dict, screenshot_file: str, is_banner: bool, app_name: str, banner_tagline: str) -> str:
    px: int = PHONE["x"]
    py: int = PHONE["y"]
    pw: int = PHONE["width"]
    ph: int = PHONE["height"]
    radius: int = PHONE["radius"]
    bezel: int = PHONE["bezel"]
    screen_x: int = px + bezel
    screen_y: int = py + bezel
    screen_w: int = pw - bezel * 2
    screen_h: int = ph - bezel * 2
    screen_radius: int = PHONE["screen_radius"]
    href: str = screenshot_href(screenshot_file)
    shadow_id: str = "phoneShadow"
    screen_content: str
    if href:
        screen_content = (
            f'<image href="{href}" x="{screen_x}" y="{screen_y}" '
            f'width="{screen_w}" height="{screen_h}" '
            f'preserveAspectRatio="xMidYMid slice" clip-path="url(#screenClip)"/>'
        )
    elif is_banner:
        screen_content = f"""
        <rect x="{screen_x}" y="{screen_y}" width="{screen_w}" height="{screen_h}"
              rx="{screen_radius}" fill="{brand['accent']}"/>
        <text x="{screen_x + screen_w // 2}" y="{screen_y + screen_h // 2 - 40}"
              text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
              font-size="72" font-weight="bold" fill="{brand['accentDark']}">{escape(app_name)}</text>
        <text x="{screen_x + screen_w // 2}" y="{screen_y + screen_h // 2 + 50}"
              text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
              font-size="40" fill="{brand['text']}">{escape(banner_tagline)}</text>
        """
    else:
        screen_content = placeholder_svg(screen_x, screen_y, screen_w, screen_h, screenshot_file)
    return f"""
    <defs>
      <filter id="{shadow_id}" x="-10%" y="-5%" width="120%" height="115%">
        <feDropShadow dx="0" dy="12" stdDeviation="24" flood-color="#000000" flood-opacity="0.15"/>
      </filter>
      <clipPath id="screenClip">
        <rect x="{screen_x}" y="{screen_y}" width="{screen_w}" height="{screen_h}" rx="{screen_radius}"/>
      </clipPath>
    </defs>
    <rect x="{px}" y="{py}" width="{pw}" height="{ph}" rx="{radius}"
          fill="{brand['phoneFrame']}" filter="url(#{shadow_id})"/>
    <rect x="{screen_x}" y="{screen_y}" width="{screen_w}" height="{screen_h}"
          rx="{screen_radius}" fill="#FFFFFF"/>
    {screen_content}
    """


def caption_accent_y(line_count: int, start_y: int, line_height: int) -> int:
    if line_count == 1:
        return 200
    last_baseline: int = start_y + (line_count - 1) * line_height
    return last_baseline + 22


def caption_block(caption: str, brand: dict) -> str:
    lines: list[str] = wrap_caption(caption)
    line_height: int = 58
    start_y: int = 120 + (2 - len(lines)) * 20
    text_lines: str = ""
    for index, line in enumerate(lines):
        y: int = start_y + index * line_height
        text_lines += (
            f'<text x="540" y="{y}" text-anchor="middle" '
            f'font-family="Segoe UI, Arial, sans-serif" font-size="48" '
            f'font-weight="600" fill="{brand["text"]}">{escape(line)}</text>\n'
        )
    accent_width: int = min(len(caption) * 14, 400)
    accent_x: int = 540 - accent_width // 2
    accent_y: int = caption_accent_y(len(lines), start_y, line_height)
    return f"""
    {text_lines}
    <rect x="{accent_x}" y="{accent_y}" width="{accent_width}" height="6" rx="3" fill="{brand['accent']}"/>
    """


def build_screenshot_svg(
    caption: str,
    screenshot_file: str,
    brand: dict,
    is_banner: bool,
    app_name: str,
    banner_tagline: str,
) -> str:
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
  <!-- layer: background -->
  <rect width="1080" height="1920" fill="{brand['background']}"/>
  <!-- layer: caption -->
  {caption_block(caption, brand)}
  <!-- layer: phone -->
  {phone_frame_svg(brand, screenshot_file, is_banner, app_name, banner_tagline)}
</svg>
"""


def build_feature_graphic_svg(
    title: str,
    subtitle: str,
    badge: str,
    brand: dict,
    icon_path: Path,
) -> str:
    layout: dict = FEATURE_GRAPHIC
    icon_href: str = icon_path.resolve().as_uri()
    icon_x: int = layout["icon_x"]
    icon_y: int = layout["icon_y"]
    icon_size: int = layout["icon_size"]
    icon_radius: int = round(icon_size * 24 / 108)
    title_y: int = layout["title_y"] + 72
    subtitle_y: int = layout["subtitle_y"] + 34
    decor_circles: str = ""
    for center_x, center_y, radius, alpha in FEATURE_DECOR_CIRCLES:
        decor_circles += (
            f'  <circle cx="{center_x}" cy="{center_y}" r="{radius}" '
            f'fill="{brand["accent"]}" opacity="{alpha}"/>\n'
        )
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
  <rect width="1024" height="500" fill="{brand['background']}"/>
{decor_circles}  <rect x="0" y="{layout['footer_y']}" width="1024" height="80" fill="{brand['accent']}"/>
  <text x="512" y="468" text-anchor="middle"
        font-family="Segoe UI, Arial, sans-serif" font-size="32" font-weight="600"
        fill="{brand['accentDark']}">{escape(badge)}</text>
  <defs>
    <filter id="iconShadow" x="-10%" y="-10%" width="130%" height="140%">
      <feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000000" flood-opacity="0.10"/>
    </filter>
    <clipPath id="iconClip">
      <rect x="{icon_x}" y="{icon_y}" width="{icon_size}" height="{icon_size}" rx="{icon_radius}"/>
    </clipPath>
  </defs>
  <image href="{icon_href}" x="{icon_x}" y="{icon_y}" width="{icon_size}" height="{icon_size}"
         clip-path="url(#iconClip)" filter="url(#iconShadow)"/>
  <text x="{layout['text_x']}" y="{title_y}" font-family="Segoe UI, Arial, sans-serif"
        font-size="96" font-weight="bold" fill="{brand['text']}">{escape(title)}</text>
  <text x="{layout['text_x']}" y="{subtitle_y}" font-family="Segoe UI, Arial, sans-serif"
        font-size="44" fill="{brand['text']}">{escape(subtitle)}</text>
</svg>
"""


def build_uac_banner_svg(
    headline: str,
    subtitle: str,
    cta: str,
    brand: dict,
    icon_path: Path,
) -> str:
    icon_href: str = icon_path.resolve().as_uri()
    headline_size: int = 96 if len(headline) <= 12 else 64
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="628" viewBox="0 0 1200 628">
  <rect width="1200" height="628" fill="{brand['background']}"/>
  <rect x="0" y="0" width="12" height="628" fill="{brand['accent']}"/>
  <image href="{icon_href}" x="60" y="60" width="100" height="100"/>
  <text x="60" y="240" font-family="Segoe UI, Arial, sans-serif"
        font-size="{headline_size}" font-weight="bold" fill="{brand['text']}">{escape(headline)}</text>
  <text x="60" y="310" font-family="Segoe UI, Arial, sans-serif"
        font-size="36" fill="{brand['text']}">{escape(subtitle)}</text>
  <rect x="60" y="380" width="280" height="72" rx="36" fill="{brand['accent']}"/>
  <text x="200" y="428" text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
        font-size="32" font-weight="600" fill="{brand['accentDark']}">{escape(cta)}</text>
  <rect x="700" y="80" width="420" height="468" rx="32" fill="{brand['phoneFrame']}"/>
  <rect x="720" y="100" width="380" height="428" rx="24" fill="#FFFFFF"
        stroke="#C4C7C7" stroke-width="2" stroke-dasharray="12 8"/>
  <text x="910" y="300" text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
        font-size="28" fill="#747878">скриншот игры</text>
  <text x="910" y="340" text-anchor="middle" font-family="Segoe UI, Arial, sans-serif"
        font-size="22" fill="#747878">(вставить в Figma)</text>
</svg>
"""


def save_svg(content: str, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def render_png(svg_path: Path, png_path: Path) -> None:
    if cairosvg is None:
        raise RuntimeError("cairosvg is required for PNG export. Install: pip install cairosvg")
    png_path.parent.mkdir(parents=True, exist_ok=True)
    cairosvg.svg2png(url=str(svg_path.resolve()), write_to=str(png_path.resolve()))


def generate_svg_sources(config: dict) -> list[Path]:
    brand: dict = config["brand"]
    icon_path: Path = ensure_icon_png(SOURCES)
    generated: list[Path] = []
    for locale, caption_key in [("ru", "captionRu"), ("en", "captionEn")]:
        locale_dir: Path = SOURCES / "screenshots" / locale
        app_name: str = brand_name(brand, locale)
        tagline: str = banner_tagline(locale)
        for item in enabled_screenshots(config):
            screenshot_file: str = f"{locale}/{item['file']}"
            svg_content: str = build_screenshot_svg(
                caption=item[caption_key],
                screenshot_file=screenshot_file,
                brand=brand,
                is_banner=item.get("isBanner", False),
                app_name=app_name,
                banner_tagline=tagline,
            )
            path: Path = locale_dir / f"{item['id']}.svg"
            save_svg(svg_content, path)
            generated.append(path)
    fg: dict = config["featureGraphic"]
    for locale, subtitle_key, badge_key in [
        ("ru", "subtitleRu", "badgeRu"),
        ("en", "subtitleEn", "badgeEn"),
    ]:
        path = SOURCES / "feature-graphic" / f"feature-graphic-{locale}.svg"
        save_svg(
            build_feature_graphic_svg(
                feature_title(fg, locale),
                fg[subtitle_key],
                fg[badge_key],
                brand,
                icon_path,
            ),
            path,
        )
        generated.append(path)
    for index, banner in enumerate(config["uacBanners"], start=1):
        for locale, subtitle_key, cta_key in [
            ("ru", "subtitleRu", "ctaRu"),
            ("en", "subtitleEn", "ctaEn"),
        ]:
            headline_key: str = "headline" if "headline" in banner else f"headline{locale.capitalize()}"
            headline: str = banner[headline_key]
            path = SOURCES / "uac-banners" / f"uac-{index:02d}-{locale}.svg"
            save_svg(
                build_uac_banner_svg(
                    headline=headline,
                    subtitle=banner[subtitle_key],
                    cta=banner[cta_key],
                    brand=brand,
                    icon_path=icon_path,
                ),
                path,
            )
            generated.append(path)
    return generated


def render_all_pngs(svg_paths: list[Path]) -> list[Path]:
    rendered: list[Path] = []
    for svg_path in svg_paths:
        relative: Path = svg_path.relative_to(SOURCES)
        png_path: Path = OUTPUT / relative.with_suffix(".png")
        render_png(svg_path, png_path)
        rendered.append(png_path)
    return rendered


def main() -> None:
    parser = argparse.ArgumentParser(description="Build Google Play marketing assets")
    parser.add_argument("--svg-only", action="store_true", help="Generate SVG sources only")
    parser.add_argument("--png-only", action="store_true", help="Render PNG from existing SVG")
    args = parser.parse_args()
    config: dict = load_config()
    SCREENSHOTS.mkdir(parents=True, exist_ok=True)
    remove_disabled_screenshot_assets(config)
    disabled_ids: set[str] = {item["id"] for item in config["screenshots"] if item.get("disabled")}
    ensure_icon_png(SOURCES)
    if args.png_only:
        svg_paths: list[Path] = list(SOURCES.rglob("*.svg"))
        svg_paths = [
            path for path in svg_paths
            if path.name not in {"icon.svg", "icon.png"} and path.stem not in disabled_ids
        ]
    else:
        svg_paths = generate_svg_sources(config)
        print(f"Generated {len(svg_paths)} SVG sources in {SOURCES}")
    if not args.svg_only:
        if cairosvg is not None:
            png_paths: list[Path] = render_all_pngs(svg_paths)
            renderer: str = "cairosvg"
        else:
            png_paths = render_all_from_config(config, SOURCES, SCREENSHOTS, OUTPUT)
            renderer = "pillow"
        print(f"Rendered {len(png_paths)} PNG files to {OUTPUT} ({renderer})")
        for png_path in png_paths:
            print(f"  {png_path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
