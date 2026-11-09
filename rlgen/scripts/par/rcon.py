import socket, struct, sys
def pkt(s, i, t, body):
    d = struct.pack('<ii', i, t) + body.encode() + b'\x00\x00'
    s.sendall(struct.pack('<i', len(d)) + d)
def recv(s):
    ln = struct.unpack('<i', s.recv(4))[0]
    d = b''
    while len(d) < ln: d += s.recv(ln - len(d))
    return d[8:-2].decode(errors='replace')
s = socket.create_connection(('127.0.0.1', 25575), timeout=600)
pkt(s, 1, 3, 'test'); recv(s)
for cmd in sys.argv[1:]:
    pkt(s, 2, 2, cmd); print('>', cmd, '\n', recv(s), flush=True)
