#      Treble Info
#      Copyright (C) 2023 Hackintosh Five
#
#      This program is free software: you can redistribute it and/or modify
#      it under the terms of the GNU General Public License as published by
#      the Free Software Foundation, either version 3 of the License, or
#      (at your option) any later version.
#
#      This program is distributed in the hope that it will be useful,
#      but WITHOUT ANY WARRANTY; without even the implied warranty of
#      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#      GNU General Public License for more details.
#
#      You should have received a copy of the GNU General Public License
#      along with this program.  If not, see <https://www.gnu.org/licenses/>.

import json

import aiohttp

BASE_ICON_URL = "https://materialdesignicons.com/api/download/icon/vectordrawable/"
META_JSON = "https://raw.githubusercontent.com/Templarian/MaterialDesign/master/meta.json"


async def fetch_meta(session: aiohttp.ClientSession) -> list[dict]:
    async with session.get(META_JSON) as response:
        return await response.json()


def write_icon(icon_path: str, icon: str):
    with open(icon_path, "wb") as icon_file:
        icon_file.write("<!-- File auto-synced, do not edit! MaterialDesignIcons ID: " + icon + "-->\n")


async def main():
    with open("icons.json") as icons_file:
        icons = json.load(icons_file)
    async with aiohttp.ClientSession() as session:
        meta = await fetch_meta(session)
        icon_map = {}
        for icon in meta:
            icon_map[icon["name"]] = icon
        for icon_path, icon_name in icons.items():
            icon = icon_map[icon_name]
            if icon["deprecated"]:
                print("Icon", icon_name, "is deprecated.")
            write_icon(icon_path, icon["id"])
