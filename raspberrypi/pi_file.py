# -*- coding:utf-8 -*-
# 
# Author: YIN MIAO
# Time: 2019/11/13 20:09

from bluetooth import *
from multiprocessing import Process, Queue
import time
#from PyOBEX.client import Client
from picamera import PiCamera
import Adafruit_DHT
import subprocess
import datetime


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


def client(q, q_log, addr):

    print("Searching for SampleServer_pc on %s" % addr)

    # search for the SampleServer service
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    while True:
        service_matches = find_service( uuid = uuid, address = addr )

        if len(service_matches) == 0:
            print("Couldn't find the SampleServer_pc service. Continue to search...")
            time.sleep(2)
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
    while True:
        data = q.get()
        if len(data) == 0: continue
        print('received data from pc:', data)
        if data == 'pic':
            while not q_log.empty():
                print('pic_begin')
                log = q_log.get()
                t = log['time']
                temp = log['temperature']
                hum = log['humidity']
                
                sock.send('time:{}, temperature:{:.2f}*C, humidity:{:.2f}%'.format(t, temp, hum))
                time.sleep(1)
                sock.send('pic_begin')
                with open ('log/{}.jpg'.format(t), 'rb') as f:
                    buffer = 1
                    while buffer:
                        buffer = f.read(1024)
                        sock.send(buffer)
                time.sleep(1)
                sock.send('pic_end')
            sock.send('transmit_end')
            print('transmit_end')
    sock.close()


def record(q_log):
    log_path = './log'
    if os.path.exists(log_path):
        subprocess.call('rm -rf {}'.format(log_path), shell=True)
    os.mkdir(log_path)

        
    print('begin motering...')
    cam = PiCamera()
    cam.resolution = (1024, 768)
    sensor = Adafruit_DHT.DHT22

    pin = '4'
    
    count = 0
    while True:
        while True:
            humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)
            if humidity is not None and temperature is not None:
                break
        if temperature > 20.0:
            t = datetime.datetime.now()
            t_fm = '{:02d}:{:02d}:{:02d}_{:02d}-{:02d}-{:04d}'.format(t.hour,
                t.minute, t.second, t.month, t.day, t.year)

            pic_name = '{}.jpg'.format(t_fm)
            pic_path = os.path.join(log_path, pic_name)

            print('taking photo...')
            #cam.start_preview()

            cam.capture(pic_path, resize=(512, 384))
            time.sleep(1)
    
            print('*ALERT* Temp={0:0.1f}*C  Humidity={1:0.1f}%'.format(temperature, humidity))
            data = {'time':t_fm, 'temperature':temperature,
                'humidity':humidity}
            q_log.put(data)

        time.sleep(15)



if __name__ == '__main__':
    addr = 'A4:70:D6:B6:48:69'
    q = Queue()
    q_log = Queue()
    p_server = Process(target=server, args=(q,))
    p_client = Process(target=client, args=(q, q_log, addr))
    p_record = Process(target=record, args=(q_log,))
    p_record.start()
    p_server.start()
    time.sleep(5)
    p_client.start()
    p_server.join()
    p_record.join()
    p_client.join()
