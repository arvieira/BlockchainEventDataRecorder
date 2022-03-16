import sys
from iroha import IrohaCrypto

if len(sys.argv) == 2:
    # these first two lines are enough to create the keys
    private_key = IrohaCrypto.private_key()
    public_key = IrohaCrypto.derive_public_key(private_key)

    # the rest of the code writes them into the file
    with open(f'./keygen_output/veh_{sys.argv[1]}.priv', 'wb') as f:
        f.write(private_key)

    with open(f'./keygen_output/veh_{sys.argv[1]}.pub', 'wb') as f:
        f.write(public_key)
    
    with open(f'./keygen_output/veh_{sys.argv[1]}@traffic.priv', 'wb') as f:
        f.write(private_key)

    with open(f'./keygen_output/veh_{sys.argv[1]}@traffic.pub', 'wb') as f:
        f.write(public_key)

else:
    print("Usage: python3 key_gen.py <number of vehicle>")
