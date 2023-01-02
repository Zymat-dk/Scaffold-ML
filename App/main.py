from kivy.app import App
from kivy.uix.image import Image


class MainApp(App):
    def build(self):
        img = Image(
            source=R"C:\Users\danie\AppData\Local\Packages\Microsoft.WindowsTerminal_8wekyb3d8bbwe\RoamingState\middle-machine-bg.jpg",
            size_hint=(1, 0.5),
            pos_hint={"center_x": 0.5, "center_y": 0.5},
        )

        return img


if __name__ == "__main__":
    app = MainApp()
    app.run()
