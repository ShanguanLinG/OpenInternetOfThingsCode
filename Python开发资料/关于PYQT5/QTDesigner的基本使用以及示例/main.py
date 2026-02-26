import sys

from PyQt5.QtWidgets import QApplication, QMainWindow
from qtdesigner import Ui_Form


# 在外部引用UI类的写法，导入Ui_Form
# UI与业务逻辑分离，耦合度更低
class MyWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.ui = Ui_Form()
        self.ui.setupUi(self)
        # 以后要添加业务逻辑必须在setupUi后面添加
        # 记得养成好习惯写上self，使属性成为类的属性方便函数进行修改


if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = MyWindow()
    window.show()
    sys.exit(app.exec_())
