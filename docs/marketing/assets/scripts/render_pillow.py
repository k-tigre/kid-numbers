"""Pillow-based PNG renderer (fallback when cairosvg is unavailable)."""
from __future__ import annotations
import textwrap
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

PHONE = {
    "x": 90,
    "y": 280,
    "width": 900,
    "height": 1580,
    "bezel": 16,
    "screen_radius": 36,
    "radius": 48,
}


def hex_to_rgb(hex_color: str) -> tuple[int, int, int]:
    value: str = hex_color.lstrip("#")
    if len(value) == 8:
        value = value[:6]
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4))


def ensure_icon_png(sources_dir: Path) -> Path:
    png_path: Path = sources_dir / "icon.png"
    if png_path.exists():
        return png_path
    size: int = 512
    canvas: Image.Image = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=96, fill=(255, 255, 255, 255))
    gray: tuple[int, int, int] = (128, 129, 132)
    cell: int = size // 4
    padding: int = size // 8
    for row in range(2):
        for col in range(2):
            x0: int = padding + col * (cell + padding // 2)
            y0: int = padding + row * (cell + padding // 2)
            draw.rounded_rectangle(
                (x0, y0, x0 + cell, y0 + cell),
                radius=16,
                outline=gray,
                width=8,
            )
    plus_center: tuple[int, int] = (padding + cell // 2, padding + cell // 2)
    draw.line(
        (plus_center[0] - 40, plus_center[1], plus_center[0] + 40, plus_center[1]),
        fill=gray,
        width=12,
    )
    draw.line(
        (plus_center[0], plus_center[1] - 40, plus_center[0], plus_center[1] + 40),
        fill=gray,
        width=12,
    )
    minus_y: int = padding + cell + padding // 2 + cell // 2
    draw.line((size - padding - cell, minus_y - 8, size - padding, minus_y - 8), fill=gray, width=12)
    draw.line((size - padding - cell, minus_y + 8, size - padding, minus_y + 8), fill=gray, width=12)
    multiply_center: tuple[int, int] = (padding + cell // 2, padding + cell + padding // 2 + cell // 2)
    for dx, dy in [(-30, -30), (30, 30), (-30, 30), (30, -30)]:
        draw.line(
            (multiply_center[0] + dx, multiply_center[1] + dy, multiply_center[0] - dx, multiply_center[1] - dy),
            fill=gray,
            width=10,
        )
    png_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(png_path, "PNG")
    return png_path


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates: list[str] = []
    if bold:
        candidates.extend(["arialbd.ttf", "segoeuib.ttf", "calibrib.ttf"])
    else:
        candidates.extend(["arial.ttf", "segoeui.ttf", "calibri.ttf"])
    for name in candidates:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def wrap_text(text: str, max_chars: int = 28) -> list[str]:
    lines: list[str] = textwrap.wrap(text, width=max_chars)
    return lines if lines else [text]


def rounded_rectangle(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    radius: int,
    fill: tuple[int, int, int] | None = None,
    outline: tuple[int, int, int] | None = None,
    width: int = 1,
) -> None:
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def draw_centered_text(
    draw: ImageDraw.ImageDraw,
    text: str,
    center_x: int,
    y: int,
    font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
    fill: tuple[int, int, int],
) -> None:
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width: int = bbox[2] - bbox[0]
    draw.text((center_x - text_width // 2, y), text, font=font, fill=fill)


def paste_screenshot(
    canvas: Image.Image,
    screenshot_path: Path,
    screen_box: tuple[int, int, int, int],
    radius: int,
) -> None:
    screen_w: int = screen_box[2] - screen_box[0]
    screen_h: int = screen_box[3] - screen_box[1]
    screenshot: Image.Image = Image.open(screenshot_path).convert("RGBA")
    screenshot_ratio: float = screenshot.width / screenshot.height
    target_ratio: float = screen_w / screen_h
    if screenshot_ratio > target_ratio:
        new_height: int = screenshot.height
        new_width: int = int(new_height * target_ratio)
        left: int = (screenshot.width - new_width) // 2
        screenshot = screenshot.crop((left, 0, left + new_width, new_height))
    else:
        new_width = screenshot.width
        new_height = int(new_width / target_ratio)
        top: int = (screenshot.height - new_height) // 2
        screenshot = screenshot.crop((0, top, new_width, top + new_height))
    screenshot = screenshot.resize((screen_w, screen_h), Image.Resampling.LANCZOS)
    mask: Image.Image = Image.new("L", (screen_w, screen_h), 0)
    mask_draw: ImageDraw.ImageDraw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle((0, 0, screen_w, screen_h), radius=radius, fill=255)
    canvas.paste(screenshot, (screen_box[0], screen_box[1]), mask)


def draw_placeholder(
    draw: ImageDraw.ImageDraw,
    screen_box: tuple[int, int, int, int],
    label: str,
    text_color: tuple[int, int, int],
    fill_color: tuple[int, int, int],
) -> None:
    rounded_rectangle(draw, screen_box, radius=8, fill=fill_color, outline=text_color, width=3)
    center_x: int = (screen_box[0] + screen_box[2]) // 2
    center_y: int = (screen_box[1] + screen_box[3]) // 2
    font_main: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(36)
    font_sub: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(28)
    draw_centered_text(draw, "Вставьте скриншот", center_x, center_y - 40, font_main, text_color)
    draw_centered_text(draw, label, center_x, center_y + 10, font_sub, text_color)


def draw_phone_frame(
    canvas: Image.Image,
    draw: ImageDraw.ImageDraw,
    brand: dict,
    screenshot_file: str,
    screenshots_dir: Path,
    is_banner: bool,
) -> None:
    px: int = PHONE["x"]
    py: int = PHONE["y"]
    pw: int = PHONE["width"]
    ph: int = PHONE["height"]
    bezel: int = PHONE["bezel"]
    frame_color: tuple[int, int, int] = hex_to_rgb(brand["phoneFrame"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    shadow: Image.Image = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    shadow_draw: ImageDraw.ImageDraw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        (px + 4, py + 12, px + pw + 4, py + ph + 12),
        radius=PHONE["radius"],
        fill=(0, 0, 0, 38),
    )
    canvas.alpha_composite(shadow)
    rounded_rectangle(draw, (px, py, px + pw, py + ph), radius=PHONE["radius"], fill=frame_color)
    screen_box: tuple[int, int, int, int] = (
        px + bezel,
        py + bezel,
        px + pw - bezel,
        py + ph - bezel,
    )
    rounded_rectangle(draw, screen_box, radius=PHONE["screen_radius"], fill=(255, 255, 255))
    screenshot_path: Path = screenshots_dir / screenshot_file
    if screenshot_path.exists():
        paste_screenshot(canvas, screenshot_path, screen_box, PHONE["screen_radius"])
    elif is_banner:
        rounded_rectangle(draw, screen_box, radius=PHONE["screen_radius"], fill=accent)
        draw_centered_text(draw, "Numbers", (screen_box[0] + screen_box[2]) // 2, screen_box[1] + screen_box[3] // 2 - 60, load_font(72, True), accent_dark)
        draw_centered_text(draw, "Free · No ads · RU/EN", (screen_box[0] + screen_box[2]) // 2, screen_box[1] + screen_box[3] // 2 + 30, load_font(40), text_color)
    else:
        draw_placeholder(draw, screen_box, screenshot_file, hex_to_rgb("#747878"), hex_to_rgb("#F1EDEC"))


def render_screenshot_png(
    caption: str,
    screenshot_file: str,
    brand: dict,
    screenshots_dir: Path,
    output_path: Path,
    is_banner: bool = False,
) -> None:
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    canvas: Image.Image = Image.new("RGBA", (1080, 1920), background + (255,))
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    lines: list[str] = wrap_text(caption)
    caption_font: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(48, True)
    line_height: int = 58
    start_y: int = 120 + (2 - len(lines)) * 20
    for index, line in enumerate(lines):
        draw_centered_text(draw, line, 540, start_y + index * line_height, caption_font, text_color)
    accent_width: int = min(len(caption) * 14, 400)
    draw.rounded_rectangle(
        (540 - accent_width // 2, 200, 540 + accent_width // 2, 206),
        radius=3,
        fill=accent,
    )
    draw_phone_frame(canvas, draw, brand, screenshot_file, screenshots_dir, is_banner)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(output_path, "PNG")


def render_feature_graphic_png(
    title: str,
    subtitle: str,
    badge: str,
    brand: dict,
    icon_path: Path,
    output_path: Path,
) -> None:
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    canvas: Image.Image = Image.new("RGB", (1024, 500), background)
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 420, 1024, 500), fill=accent)
    draw_centered_text(draw, badge, 512, 438, load_font(32, True), accent_dark)
    if icon_path.exists():
        icon: Image.Image = Image.open(icon_path).convert("RGBA")
        icon = icon.resize((180, 180), Image.Resampling.LANCZOS)
        canvas.paste(icon, (80, 110), icon)
    draw.text((300, 120), title, font=load_font(96, True), fill=text_color)
    draw.text((300, 230), subtitle, font=load_font(44), fill=text_color)
    draw.ellipse((760, 80, 1000, 320), fill=accent + (64,))
    draw_centered_text(draw, "+ − × ÷", 880, 175, load_font(64, True), accent_dark)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path, "PNG")


def render_uac_banner_png(
    headline: str,
    subtitle: str,
    cta: str,
    brand: dict,
    icon_path: Path,
    output_path: Path,
) -> None:
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    frame_color: tuple[int, int, int] = hex_to_rgb(brand["phoneFrame"])
    canvas: Image.Image = Image.new("RGB", (1200, 628), background)
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 0, 12, 628), fill=accent)
    if icon_path.exists():
        icon: Image.Image = Image.open(icon_path).convert("RGBA")
        icon = icon.resize((100, 100), Image.Resampling.LANCZOS)
        canvas.paste(icon, (60, 60), icon)
    headline_size: int = 96 if len(headline) <= 12 else 64
    draw.text((60, 170), headline, font=load_font(headline_size, True), fill=text_color)
    draw.text((60, 280), subtitle, font=load_font(36), fill=text_color)
    draw.rounded_rectangle((60, 380, 340, 452), radius=36, fill=accent)
    draw_centered_text(draw, cta, 200, 396, load_font(32, True), accent_dark)
    rounded_rectangle(draw, (700, 80, 1120, 548), radius=32, fill=frame_color)
    rounded_rectangle(draw, (720, 100, 1100, 528), radius=24, fill=(255, 255, 255), outline=hex_to_rgb("#C4C7C7"), width=2)
    draw_centered_text(draw, "скриншот игры", 910, 290, load_font(28), hex_to_rgb("#747878"))
    draw_centered_text(draw, "(вставить в Figma)", 910, 330, load_font(22), hex_to_rgb("#747878"))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path, "PNG")


def render_all_from_config(
    config: dict,
    sources_dir: Path,
    screenshots_dir: Path,
    output_dir: Path,
) -> list[Path]:
    brand: dict = config["brand"]
    icon_path: Path = ensure_icon_png(sources_dir)
    rendered: list[Path] = []
    for locale, caption_key in [("ru", "captionRu"), ("en", "captionEn")]:
        for item in config["screenshots"]:
            if item.get("disabled"):
                continue
            output_path: Path = output_dir / "screenshots" / locale / f"{item['id']}.png"
            render_screenshot_png(
                caption=item[caption_key],
                screenshot_file=f"{locale}/{item['file']}",
                brand=brand,
                screenshots_dir=screenshots_dir,
                output_path=output_path,
                is_banner=item.get("isBanner", False),
            )
            rendered.append(output_path)
    fg: dict = config["featureGraphic"]
    for locale, subtitle_key, badge_key in [
        ("ru", "subtitleRu", "badgeRu"),
        ("en", "subtitleEn", "badgeEn"),
    ]:
        output_path = output_dir / "feature-graphic" / f"feature-graphic-{locale}.png"
        render_feature_graphic_png(
            title=fg["title"],
            subtitle=fg[subtitle_key],
            badge=fg[badge_key],
            brand=brand,
            icon_path=icon_path,
            output_path=output_path,
        )
        rendered.append(output_path)
    for index, banner in enumerate(config["uacBanners"], start=1):
        for locale, subtitle_key, cta_key in [
            ("ru", "subtitleRu", "ctaRu"),
            ("en", "subtitleEn", "ctaEn"),
        ]:
            headline_key: str = "headline" if "headline" in banner else f"headline{locale.capitalize()}"
            output_path = output_dir / "uac-banners" / f"uac-{index:02d}-{locale}.png"
            render_uac_banner_png(
                headline=banner[headline_key],
                subtitle=banner[subtitle_key],
                cta=banner[cta_key],
                brand=brand,
                icon_path=icon_path,
                output_path=output_path,
            )
            rendered.append(output_path)
    return rendered
