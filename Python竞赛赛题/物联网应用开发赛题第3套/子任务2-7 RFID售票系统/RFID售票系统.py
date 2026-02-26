"""
物联网应用开发赛题第三套
子任务2-7 RFID售票系统
作者 ShanguanLinG
完成时间 2026/01/05
因为超高频桌面读卡器不知道怎么使用所以就写了个通用的add_ticket用于代表售票操作
本质上仍然是数据处理 如果知道怎么使用的话把读取到的数据添加进add_ticket方法即可
记得使用qt的自定义信号, 在其他线程进行GUI操作会导致Qt报错, 用信号传递可以避免这个问题
"""

import json
import sys

from nle_library.databus.SocketDataBus import SocketDataBus
from nle_library.device.GenericConnector import GenericConnector

from PyQt5 import QtCore
from PyQt5.QtCore import QObject, QTimer, pyqtSignal, pyqtBoundSignal
from PyQt5.QtWidgets import (
    QApplication,
    QMainWindow,
    QTableWidgetItem,
    QPushButton,
    QTableWidget,
)

from my_ui import Ui_MainWindow

# 表格显示与列索引常量

SEATED_YES = "是"  # 已入座
SEATED_NO = "否"  # 未入座

COL_SEAT_NUM = 0  # 座位号列
COL_RFID = 1  # RFID列
COL_SEATED = 2  # 入座状态列
COL_ACTION = 3  # 操作列(退票按钮)


class Ticket:
    # seat_num: 座位号
    # rfid: RFID字符串 (票据/标签编号)
    # is_seated: 是否已入座
    # is_refund: 是否已退票

    def __init__(self, seat_num: int, rfid: str, is_seated: bool, is_refund: bool):
        self.seat_num = seat_num
        self.rfid = rfid
        self.is_seated = is_seated
        self.is_refund = is_refund


# 表格管理器, 用于专门处理表格的业务逻辑
class TableManager(QObject):
    def __init__(self, table: QTableWidget):
        # 初始化表格, 传入表格参数并方便后续调用
        super().__init__()
        self.table = table

    def get_first_empty_seat_num(self) -> int:
        # 查找表格中第一个空的位置
        # 通过逐行遍历rfid列, 直到为空并返回行数量
        for row in range(self.table.rowCount()):
            item = self.table.item(row, COL_SEAT_NUM)
            if item is None:
                return row
        # 如果所有行都有座位号，说明表格可能已满
        return self.table.rowCount()

    def add_ticket(self, ticket: Ticket) -> None:
        # 将一张票添加到表格中的逻辑实现

        # 如果已存在相同的rfid, 则直接返回
        if self.rfid_exists(ticket.rfid):
            return
        # 得到第一个空的表格位置
        row = self.get_first_empty_seat_num()
        # 进行添加操作
        self.table.setItem(row, COL_SEAT_NUM, QTableWidgetItem(str(ticket.seat_num)))
        self.table.setItem(row, COL_RFID, QTableWidgetItem(ticket.rfid))
        self.table.setItem(
            row, COL_SEATED, QTableWidgetItem(SEATED_YES if ticket.is_seated else SEATED_NO)
        )
        # 创建按钮对象
        button = QPushButton("退票")
        # 绑定按钮事件
        self.table.setCellWidget(row, COL_ACTION, button)
        # 用户点击退票按钮时触发on_click(在UI线程)
        button.clicked.connect(self.on_click)

    def rfid_exists(self, rfid: str) -> bool:
        # 查找表格中是否存在某个RFID
        for row in range(self.table.rowCount()):
            item = self.table.item(row, COL_RFID)
            if item is None:
                continue
            if rfid == item.text():
                return True
        return False

    def seat_ticket(self, rfid: str) -> None:
        # 模拟检票逻辑
        # 根据rfid值所在行, 将座位号的是否入座状态变更为是。
        if not self.rfid_exists(rfid):
            return

        row = self.get_row_form_rfid(rfid)
        if row == -1:
            return

        self.table.setItem(row - 1, COL_RFID, None)
        self.table.setItem(row - 1, COL_SEATED, QTableWidgetItem(SEATED_YES))
        self.table.setCellWidget(row - 1, COL_ACTION, None)

    def get_row_form_rfid(self, rfid: str) -> int:
        # 根据 RFID 查找其所在行, 返回的是行号(从1开始)而并非索引
        row_index = 0
        for row in range(self.table.rowCount()):
            row_index += 1
            item = self.table.item(row, COL_RFID)
            if item is None:
                continue
            if rfid == item.text().strip():
                return row_index
        return -1

    def get_ticket_sold_count(self) -> int:
        # 统计"已售出"的数量
        # RFID列不为空即视为已售出
        ticket_count = 0
        for row in range(self.table.rowCount()):
            if self.table.item(row, COL_RFID) is not None:
                ticket_count += 1
        return ticket_count

    def get_ticket_seat_in_count(self) -> int:
        # 统计"已入座"的数量
        # seated列文字为"是"即视为已售出
        ticket_count = 0
        for row in range(self.table.rowCount()):
            item = self.table.item(row, COL_SEATED)
            if item is None:
                continue
            if item.text().strip() == SEATED_YES:
                ticket_count += 1
        return ticket_count

    def on_click(self) -> None:
        # 退票按钮被点击后的逻辑
        button = self.sender()
        if not isinstance(button, QPushButton):
            return
        row = self.table.indexAt(button.pos()).row()
        self.table.setItem(row, COL_RFID, None)
        button.setEnabled(False)

    def refresh_table_style(self, table: QTableWidget) -> None:
        # 刷新表格样式, 将所有非空单元格内容设置为居中显示
        # P.S.只是为了好看, 且题目中的文字确实是居中显示的
        for i in range(table.rowCount()):
            for j in range(table.columnCount()):
                item = table.item(i, j)
                if item is None:
                    continue
                item.setTextAlignment(QtCore.Qt.AlignCenter)


class MyWindow(QMainWindow):
    # 主窗口类
    def __init__(self):
        super().__init__()
        self.ui = Ui_MainWindow()
        self.ui.setupUi(self)

    def setupLabel(self, all_seat_count: int, sold_count: int, seat_in_count: int) -> None:
        # 更新"总座位数", "已售出", "已入座"的数量
        self.ui.label_3.setText(str(all_seat_count))
        self.ui.label_6.setText(str(sold_count))
        self.ui.label_8.setText(str(seat_in_count))


class UHFDispatcher(QObject):
    # 为什么要用 pyqtSignal而不是直接使用函数处理数据?
    # Qt的GUI组件(QTableWidget, QLabel等)只能在UI主线程中修改
    # 第三方库的回调函数callback经常来自非UI线程, 如果直接操作UI, 容易出现刷新异常或警告
    # 使用信号/槽可以让Qt自动把执行安排到正确的线程/事件队列中, 从而稳定刷新界面
    # 人话: 不要直接在回调函数中调用Qt的GUI操作, 回调函数大概率不在主线程, 直接进行GUI操作可能导致卡死或者异常

    # 声明一个信号模板：将来实例上的 seat_signal 只能携带一个 str 参数
    # 注意：pyqtSignal 必须定义在 QObject 子类的“类属性”上
    seat_signal: pyqtBoundSignal = pyqtSignal(str)


if __name__ == "__main__":
    # 回调函数: 网关收到UHF数据后调用
    # callback来自非UI线程, 不能直接在这个回调函数中操作UI控件(重点)
    # 通过seat_signal把RFID字符串投递给UI线程, 由槽函数更新表格
    # 这个callback会接收两个参数, 第二个没有使用到(是一个布尔值), 可以填写_
    def callback(data, _):
        # 解析网关返回 JSON：提取 datas -> uhf 对应字段
        sensor_data = json.loads(data.decode()[:-2])["datas"]["uhf"]
        # 发射信号：把 RFID 字符串投递给 UI 线程处理
        dispatcher.seat_signal.emit(sensor_data)


    # 定时更新：每次向网关请求标识名为uhf的传感器
    def update():
        generic.sendGatewaySearch("uhf", callback, None)


    # 槽函数: 接收到seat_signal后执行入座逻辑(在UI主线程)
    def on_seat_ticket(sensor_data: str):
        tbm.seat_ticket(sensor_data)
        ticket_seat_in_count = tbm.get_ticket_seat_in_count()
        ticket_sold_count = tbm.get_ticket_sold_count()
        window.setupLabel(
            window.ui.tableWidget.rowCount(),
            ticket_sold_count,
            ticket_seat_in_count,
        )


    # Qt 初始化
    app = QApplication(sys.argv)
    window = MyWindow()
    window.show()
    timer = QTimer()
    tbm = TableManager(window.ui.tableWidget)
    # 手动售出一张票, 并将其添加到表格中
    tbm.add_ticket(Ticket(2, "E2 00 50 08 06 0A 00 52 09 40 B8 FE", False, False))
    dispatcher = UHFDispatcher()
    # 建立规则: seat_signal 被emit时, Qt会在合适的时机(UI线程)调用on_seat_ticket函数
    dispatcher.seat_signal.connect(on_seat_ticket)
    socket = SocketDataBus("172.18.25.16", 57500)
    generic = GenericConnector(socket)
    timer.timeout.connect(update)
    timer.start(1000)  # 1000ms, 代表每一秒循环执行一次update函数
    sys.exit(app.exec_())
