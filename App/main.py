import kivy
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout

kivy.require("2.1.0")


class Crazy(BoxLayout):
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


class Test(App):
    def build(self):
        return Crazy()


if __name__ == "__main__":
    app = Test()
    app.run()
