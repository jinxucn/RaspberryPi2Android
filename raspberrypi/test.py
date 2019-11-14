# -*- coding:utf-8 -*-
# 
# Author: YIN MIAO
# Time: 2019/11/13 16:44


import bluetooth

while True:
    nearby_devices = bluetooth.discover_devices(lookup_names=True)
    print("found %d devices" % len(nearby_devices))

    for addr, name in nearby_devices:
        print("  %s - %s" % (addr, name))