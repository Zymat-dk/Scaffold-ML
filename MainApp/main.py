import kivy
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout

kivy.require("2.1.0")


class ScaffoldList(BoxLayout):
    def __init__(self):
        super(ScaffoldList, self).__init__()


class Main(App):
    def build(self):
        return ScaffoldList()


if __name__ == '__main__':
    app = Main()
    app.run()
