import sys

# from event_manager.manager_thread import Manager
from event_manager.manager_multiprocessing import Manager
from file_handler import utils
from file_handler.utils import parse_params, read_dir

if __name__ == '__main__':
    input_dir = parse_params(sys.argv[1:])
    event_manager = Manager()
    vehicles = read_dir(input_dir)

    for veh in vehicles:
        timeline = utils.read_file(vehicles[veh])
        event_manager.add_timeline(veh, timeline)

    event_manager.start_simulation()
    event_manager.stop_simulation()
