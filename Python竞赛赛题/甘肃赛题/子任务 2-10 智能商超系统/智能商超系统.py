import json
import sys
from nle_library.device.GenericConnector import GenericConnector
from nle_library.databus.SocketDataBus import SocketDataBus

from PyQt5.QtCore import QObject, QTimer, pyqtSignal
from PyQt5.QtWidgets import QApplication, QFrame

from my_ui import Ui_Form
import json
import typing
import ctypes
import queue


class Client:
    def __init__(self, ip, port):
        self.ip = ip
        self.port = port
        self.socket: SocketDataBus = None
        self.gen: GenericConnector = None

    def connect(self):
        self.socket = SocketDataBus(self.ip, self.port)
        self.gen = GenericConnector(self.socket)


class DataPoller(QObject):
    signal = pyqtSignal(str, str)

    def __init__(self, client: Client, api_tags: list):
        super().__init__()
        self.client = client
        self.api_tags = api_tags
        self.timer = QTimer()
        self.timer.timeout.connect(self.poll)

    def poll(self):
        for api_tag in self.api_tags:
            self.client.gen.sendGatewaySearch(
                api_tag,
                lambda data, _, tag=api_tag: self.callback(data, tag),
                None
            )

    def callback(self, data, api_tag):
        value = json.loads(data.decode()[:-2])["datas"][api_tag]
        self.signal.emit(api_tag, value)

    def start(self):
        self.timer.start(1000)

    def stop(self):
        self.timer.stop()


class Window(QFrame):
    def __init__(self):
        super().__init__()
        self.ui = Ui_Form()
        self.ui.setupUi(self)


class Guest:
    def __init__(self, name, rfid, count):
        self.name = name
        self.rfid = rfid
        self.count = count


def update_ui(api_tag, value):
    if api_tag != "uhf":
        return
    if value == g1.rfid:
        window.ui.label_2.setText(g1.name)
        window.ui.label_4.setText(str(g1.count))
    if value == g2.rfid:
        window.ui.label_2.setText(g2.name)
        window.ui.label_4.setText(str(g2.count))
    if value == g3.rfid:
        window.ui.label_2.setText(g3.name)
        window.ui.label_4.setText(str(g3.count))


if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = Window()
    client = Client("172.18.23.16", 57500)
    client.connect()
    g1 = Guest("A", "E2 00 50 08 06 0A 02 66 09 30 BC B5", 24)
    g2 = Guest("B", "E2 00 50 08 06 0A 00 52 09 40 B8 FE", 30)
    g3 = Guest("C", "E2 00 41 45 28 16 00 90 04 90 DC E5", 27)
    api_tags = ["uhf"]
    poller = DataPoller(client, api_tags)
    poller.signal.connect(update_ui)
    poller.start()
    window.show()
    sys.exit(app.exec_())
