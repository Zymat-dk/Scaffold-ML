from kivymd.app import MDApp
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.button import MDRectangleFlatButton


class Crazy(MDBoxLayout):
    def __init__(self):
        super(Crazy, self).__init__()

    def do_something(self):
        num = self.testlabel.text
        try:
            num = int(num)
            num += 1
            self.testlabel.text = str(num)
        except ValueError:
            self.testlabel.text = "1"


class Test(MDApp):
    def build(self):
        self.theme_cls.material_style = "M3"
        self.theme_cls.theme_style = "Dark"
        return Crazy()


if __name__ == "__main__":
    app = Test()
    app.run()
