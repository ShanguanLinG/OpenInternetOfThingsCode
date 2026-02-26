import sys
from collections import defaultdict, deque
from nle_library.databus.SocketDataBus import SocketDataBus
from nle_library.device.GenericConnector import GenericConnector
import json

from PyQt5.QtCore import QObject, QTimer, pyqtSignal, pyqtBoundSignal
from PyQt5.QtWidgets import QApplication


# 网关数据与控制工具类

class Client:
    def __init__(self, ip, port):
        self.ip = ip
        self.port = port
        self.so: SocketDataBus = None
        self.gen: GenericConnector = None

    def connect(self):
        self.so = SocketDataBus(self.ip, self.port)
        self.gen = GenericConnector(self.so)


class DataPoller(QObject):
    signal: pyqtBoundSignal = pyqtSignal(str, str)

    def __init__(self, client: Client, api_tags: list):
        super().__init__()
        self.client: Client = client
        self.api_tags = api_tags
        self._timer = QTimer()
        self._timer.timeout.connect(self._poll)

    def _poll(self):
        for api_tag in self.api_tags:
            self.client.gen.sendGatewaySearch(
                api_tag,
                lambda data, _, tag=api_tag: self._callback(data, api_tag)
            )

    def _callback(self, data, api_tag):
        value = json.loads(data.decode()[:-2])["datas"][api_tag]
        self.signal.emit(api_tag, str(value))

    def start(self):
        self._timer.start(1000)

    def stop(self):
        self._timer.stop()


class Controller:
    def __init__(self, client: Client):
        self.client = client

    def control(self, api_tag, data):
        self.client.gen.sendGatewayControl(
            api_tag,
            "0",
            data,
            None,
            None
        )


class DataBuffer(QObject):
    signal: pyqtBoundSignal = pyqtSignal(str)

    def __init__(self, maxlen: int):
        super().__init__()
        self.maxlen = maxlen
        self._buffers = defaultdict(
            lambda: deque(maxlen=self.maxlen)
        )

    def push(self, api_tag, value):
        self._buffers[api_tag].append(value)
        self.signal.emit(api_tag)

    def get_latest(self, api_tag):
        datas = self.get_history(api_tag)
        return datas[-1] if datas else None

    def get_history(self, api_tag):
        return list(self._buffers.get(api_tag, []))


def on_value(api_tag, value):
    if api_tag == "z_temp":
        # 处理温度相关数据, 可以在这里继续调用其他函数以保持代码低耦合，此时调用的函数应该是只处理温度相关的
        print("z_temp" + ": " + value)
    if api_tag == "z_hum":
        # 湿度处理同理
        print("z_hum" + ": " + value)
    if api_tag == "z_light":
        # 光照数据采用了DataBuffer工具类，用于存储历史记录
        # push方法将数据添加进deque
        buffer.push(api_tag, value)


def on_push(api_tag):
    # 当push时调用on_push方法
    print(api_tag + "history: ", buffer.get_history(api_tag))


if __name__ == '__main__':
    app = QApplication(sys.argv)
    client = Client("172.20.16.16", 57500)
    client.connect()
    api_tags = [
        "z_temp",
        "z_hum",
        "z_light"
    ]
    poller = DataPoller(client, api_tags)
    buffer = DataBuffer(10)
    buffer.signal.connect(on_push)
    poller.signal.connect(on_value)
    poller.start()
    sys.exit(app.exec_())
