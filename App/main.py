from kivymd.app import MDApp
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.button import MDRectangleFlatButton
from kivymd.uix.bottomnavigation import MDBottomNavigation, MDBottomNavigationItem
from kivy.lang import Builder
from colors import COLORS

Builder.load_file("main.kv")



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

        self.theme_cls.theme_style = "Dark"
        self.theme_cls.theme_style_switch_animation = True
        self.theme_cls.primary_palette = "Blue"
        self.theme_cls.primary_dark_hue = "300"
        self.theme_cls.primary_light_hue = "400"
        self.theme_cls.material_style = "M3"


        self.theme_cls.colors = COLORS


        return Crazy()

    def theme_switch(self):
        self.theme_cls.theme_style = (
            "Dark" if self.theme_cls.theme_style == "Light" else "Light"
        )


if __name__ == "__main__":
    app = Test()
    app.run()
