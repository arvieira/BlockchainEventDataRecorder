import docker


def get_container_ip(container_name):
    client = docker.DockerClient()
    container = client.containers.get(container_name)
    ip_add = container.attrs['NetworkSettings']['Networks']['iroha-net']['IPAddress']
    return ip_add
