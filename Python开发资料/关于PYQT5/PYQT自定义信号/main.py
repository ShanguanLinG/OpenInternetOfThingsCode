import sys
import time

from PyQt5.QtCore import pyqtSignal, pyqtBoundSignal, QThread
from PyQt5.QtWidgets import QMainWindow, QApplication

from my_ui import Ui_Form

"""
PYQT自定义信号练习
PyQt中所有UI控件只能在GUI线程(主线程)中创建和修改
子线程不能直接操作UI; 若要更新 UI, 应通过信号发射数据, 并在主线程的槽函数中更新界面
Worker线程应只负责耗时任务, 不持有UI控件以保持解耦和线程安全
"""


class Worker(QThread):
    """
    Worker中不推荐持有UI控件
    不要在构造函数中传入类似label的参数然后在Worker方法中进行修改
    结果即使能正常运行, 这种写法也是不规范的.
    目的: 降低耦合度(Worker是业务/任务层, 不应该依赖UI)
    """
    # 定义发射信号的类型为int
    progress: pyqtBoundSignal = pyqtSignal(int)

    def __init__(self):
        super().__init__()

    # Python中重写只靠同名方法覆盖, 不需要类似Java的@Override注解
    def run(self):
        for i in range(100):
            # 休眠0.1秒(注意单位并非毫秒)
            time.sleep(0.1)
            # 向progress发射一个信号, 其值为i + 1
            self.progress.emit(i + 1)


class MyWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.ui = Ui_Form()
        self.ui.setupUi(self)
        self.worker = Worker()
        # 声明一条规则: 当progress信号被emit发射时, Qt会调用已connect的槽函数
        self.worker.progress.connect(self.on_number_change)
        # 当按钮被点击时, 开始执行新线程内的代码(从run函数开始)
        self.ui.pushButton.clicked.connect(self.worker.start)

    def on_number_change(self, num):
        self.ui.label.setText(str(num))


if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = MyWindow()
    window.show()
    sys.exit(app.exec_())
