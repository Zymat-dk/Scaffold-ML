import kivy
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.widget import Widget
from kivy.properties import ObjectProperty
from kivy.lang import Builder
from kivy.uix.tabbedpanel import TabbedPanel
from kivy.core.window import Window

kivy.require("2.1.0")


class MainWindow(TabbedPanel):
    def __init__(self):
        super(MainWindow, self).__init__()


class Main(App):
    def build(self):
        return MainWindow()


if __name__ == "__main__":
    app = Main()
    app.run()
