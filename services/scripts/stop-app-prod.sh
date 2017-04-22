#!/bin/sh

pidToKill=$(forever list | grep -v STOPPED |grep data| grep -v id |awk '{printf("%s\n",$7);}')
forever stop $pidToKill
