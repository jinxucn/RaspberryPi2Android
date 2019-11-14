# -*- coding:utf-8 -*-
# 
# Author: YIN MIAO
# Time: 2019/11/13 20:09

from bluetooth import *
from multiprocessing import Process, Queue


def server(q):

    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("", PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

    advertise_service(server_sock, "SampleServer_pc",
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
            if len(data) == 0: continue
            q.put(data)
    except IOError:
        pass

    print("disconnected")

    client_sock.close()
    server_sock.close()
    print("all done")


def client(q, addr):
    try:
        input = raw_input
    except NameError:
        pass  # Python 3

    # if len(sys.argv) < 2:
    #     print("no device specified.  Searching all nearby bluetooth devices for")
    #     print("the SampleServer service")
    # else:
    #     addr = sys.argv[1]
    #     print("Searching for SampleServer on %s" % addr)
    print("Searching for SampleServer on %s" % addr)

    # search for the SampleServer service
    uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
    while True:
        service_matches = find_service( uuid = uuid, address = addr )

        if len(service_matches) == 0:
            print("Couldn't find the SampleServer service. Continue to search...")
        # sys.exit(0)
        else:
            break

    first_match = service_matches[0]
    port = first_match["port"]
    name = first_match["name"]
    host = first_match["host"]

    print("connecting to \"%s\" on %s" % (name, host))

    # Create the client socket
    sock=BluetoothSocket( RFCOMM )
    sock.connect((host, port))

    print("connected.")
    while True:
        data = q.get()
        if len(data) == 0: continue
        print(data)
        sock.send(data)

    sock.close()


if __name__ == '__main__':
    addr = 'B8:27:EB:EA:08:F4'
    q = Queue()
    p_server = Process(target=server, args=(q,))
    p_client = Process(target=client, args=(q, addr))
    p_server.start()
    p_client.start()
    p_server.join()
    p_client.join()