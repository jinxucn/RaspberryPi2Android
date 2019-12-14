# -*- coding:utf-8 -*-
# 
# Author: YIN MIAO
# Time: 2019/11/13 16:44


import os

if os.path.isfile('log/record.log'):
    for line in open('log/record.log','r'):
        print(line)
        print('pic_begin')
        with open ('log/{}.png'.format(line[4:20]), 'rb') as f:
                buffer = 1
                while buffer:
                    buffer = f.read(1024)
                    print(buffer)
        print('pic_end')
    print('log_end')
else:
    print('no_record')