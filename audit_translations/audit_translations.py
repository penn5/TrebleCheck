#!/usr/bin/env python3

#    Treble Info
#    Copyright (C) 2019-2023 Hackintosh Five

#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU Affero General Public License as
#    published by the Free Software Foundation, either version 3 of the
#    License, or (at your option) any later version.

#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU Affero General Public License for more details.

#    You should have received a copy of the GNU Affero General Public License
#    along with this program.  If not, see <https://www.gnu.org/licenses/>.
# SPDX-License-Identifier: GPL-3.0-or-later

import poeditor
import json
import io
import sys
import googletrans
import jinja2
import webbrowser
import os.path


token = sys.argv[1]
project = int(sys.argv[2])

api = poeditor.POEditorAPI(api_token=token)
langs = api.list_project_languages(project)

data = {}
fuzzy = set()
for lang in langs:
    if not lang["translations"]:
        continue
    exported = api.view_project_terms(project, lang["code"])
    data.setdefault(lang["code"], {}).update({term["term"]: term["translation"]["content"] for term in exported})
    for term in exported:
        if term["translation"]["fuzzy"]:
            fuzzy.add((lang["code"], term["term"]))

data = json.load(open("tmp.json"))
fuzzy = {tuple(x) for x in json.load(open("3.json"))}

by_term = {}
for lang_name, lang_data in data.items():
    for term, translation in lang_data.items():
        by_term.setdefault(term, {})[lang_name] = translation

output = {}
translator = googletrans.Translator()
for term, translations in by_term.items():
    for lang, translated in zip(translations.keys(), translator.translate(list(translations.values()))):
        output.setdefault(term, {})[lang] = translated.text

environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader("templates"),
    autoescape=jinja2.select_autoescape()
)
template = environment.get_template("audit_translations.html")
rendered = template.render(terms=output, fuzzy=fuzzy)
output_path = "output.html"
with open(output_path, "w") as output_file:
    output_file.write(rendered)

webbrowser.open_new_tab("file://" + os.path.realpath(output_path))