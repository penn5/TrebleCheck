#
#     Treble Info
#     Copyright (C) 2022-2023 Hackintosh Five
#
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
# SPDX-License-Identifier: GPL-3.0-or-later

mkdir -p vendor/etc/vintf
mkdir -p vendor/etc/manifest
mkdir -p odm/etc/vintf
mkdir -p odm/etc/manifest
mkdir -p vendor/etc/selinux
vendor_sku="$(adb shell getprop ro.boot.product.vendor.sku)"
adb shell run-as tk.hack5.treblecheck cat "/vendor/etc/vintf/manifest_$(adb shell getprop ro.boot.product.vendor.sku).xml" > "vendor/etc/vintf/manifest_sku.xml" || rm "vendor/etc/vintf/manifest_sku.xml"
adb shell run-as tk.hack5.treblecheck cat "/vendor/etc/vintf/manifest.xml" > "vendor/etc/vintf/manifest.xml" || rm "vendor/etc/vintf/manifest.xml"
i=0
rm "vendor/etc/manifest/*"
adb shell run-as tk.hack5.treblecheck find "/vendor/etc/manifest/" -maxdepth 1 -iname '*.xml' | while IFS= read -r file; do
  adb shell run-as tk.hack5.treblecheck cat "$file" > "vendor/etc/manifest/$i" || rm "vendor/etc/manifest/$i"
  i=$((i+1))
done
adb shell run-as tk.hack5.treblecheck cat "/vendor/manifest.xml" > "vendor/manifest.xml" || rm "vendor/manifest.xml"
odm_sku="$(adb shell getprop ro.boot.product.hardware.sku)"
adb shell run-as tk.hack5.treblecheck cat "/odm/etc/vintf/manifest_$odm_sku.xml" > "odm/etc/vintf/manifest_sku.xml" || rm "odm/etc/vintf/manifest_sku.xml"
adb shell run-as tk.hack5.treblecheck cat "/odm/etc/vintf/manifest.xml" > "odm/etc/vintf/manifest.xml" || rm "odm/etc/vintf/manifest.xml"
adb shell run-as tk.hack5.treblecheck cat "/odm/etc/$odm_sku.xml" > "odm/etc/sku.xml" || rm "odm/etc/sku.xml"
adb shell run-as tk.hack5.treblecheck cat "/odm/etc/manifest.xml" > "odm/etc/manifest.xml" || rm "odm/etc/manifest.xml"
i=0
rm "odm/etc/manifest/*"
adb shell run-as tk.hack5.treblecheck find "/odm/etc/manifest/" -maxdepth 1 -iname '*.xml' | while IFS= read -r file; do
  adb shell run-as tk.hack5.treblecheck cat "$file" > "odm/etc/manifest/$i" || rm "odm/etc/manifest/$i"
  i=$((i+1))
done
adb shell run-as tk.hack5.treblecheck cat "/vendor/etc/vintf/compatibility_matrix.xml" > "vendor/etc/vintf/compatibility_matrix.xml" || rm "vendor/etc/vintf/compatibility_matrix.xml"
i=0
rm "vendor/etc/selinux/*"
adb shell run-as tk.hack5.treblecheck find "/vendor/etc/selinux/" -maxdepth 1 -iname '*.cil' | while IFS= read -r file; do
  adb shell run-as tk.hack5.treblecheck cat "$file" > "vendor/etc/selinux/$i" || rm "vendor/etc/selinux/$i"
  i=$((i+1))
done
adb shell run-as tk.hack5.treblecheck cat "/vendor/etc/selinux/plat_sepolicy_vers.txt" > "vendor/etc/selinux/plat_sepolicy_vers.txt" || rm "vendor/etc/selinux/plat_sepolicy_vers.txt"
