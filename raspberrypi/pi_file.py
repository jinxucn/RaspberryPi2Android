# -*- coding:utf-8 -*-
# 
# Author: YIN MIAO
# Time: 2019/11/13 20:09

from bluetooth import *
from multiprocessing import Process, Queue
import time
from PyOBEX.client import Client
from picamera import PiCamera


def server(q):

    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("", PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    advertise_service(server_sock, "SampleServer_pi",
                      service_id=uuid,
                      service_classes=[uuid, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE],
                      #                   protocols = [ OBEX_UUID ]
                      )

    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)

    try:
        while True:
            data = client_sock.recv(1024)
            print('server received data', data)
            if len(data) == 0: continue
            q.put(data)
    except IOError:
        pass

    print("disconnected")

    client_sock.close()
    server_sock.close()
    print("all done")


def client(q, addr):

    print("Searching for SampleServer_pc on %s" % addr)

    # search for the SampleServer service
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    while True:
        service_matches = find_service( uuid = uuid, address = addr )

        if len(service_matches) == 0:
            print("Couldn't find the SampleServer_pc service. Continue to search...")
            time.sleep(1)
        # sys.exit(0)
        else:
            break

    first_match = service_matches[0]
    port = first_match["port"]
    name = first_match["name"]
    host = first_match["host"]

    print("Client connecting to \"%s\" on %s" % (name, host))

    # Create the client socket
    sock = BluetoothSocket( RFCOMM )
    sock.connect((host, port))

    print("Client connected to {}.".format(name))
##    cam = PiCamera()
    while True:
        data = q.get()
        if len(data) == 0: continue
        print('received data from pc:', data)
        if data == 'pic':
##            print('taking photo...')
##            #cam.start_preview()
##            time.sleep(1)
##            cam.capture('pic.jpg')
##            print('sending image...')
##            sock.send('begin')
##            with open ('pic.png', 'rb') as f:
##                buffer = 1
##                while buffer:
##                    buffer = f.read(1024)
##                    sock.send(buffer)
##            print('image sent.')
##            sock.send('end')
            if os.path.isfile('log/record.log'):
                lf = open('log/record.log','r')
                for line in lf.readlines():
                    sock.send(line)
                    print('sending: '+line)
                    sock.send('pic_begin')
                    with open ('log/{}.png'.format(line[4:20]), 'rb') as f:
                        buffer = 1
                        while buffer:
                            buffer = f.read(1024)
                            sock.send(buffer)
                    time.sleep(1)
                    sock.send('pic_end')
                sock.send('transmit_end')
                print('transmit_end')
                lf.close()
                os.remove('log/record.log')
            else:
                sock.send('no_record')
    sock.close()


if __name__ == '__main__':
    addr = 'A4:70:D6:B6:48:69'
    q = Queue()
    p_server = Process(target=server, args=(q,))
    p_client = Process(target=client, args=(q, addr))
    p_server.start()
    time.sleep(5)
    p_client.start()
    p_server.join()
    p_client.join()
