from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.button import Button


class Home(BoxLayout):
    def __init__(self, **kwargs):
        super().__init__(orientation='vertical', padding=24, spacing=12, **kwargs)
        self.add_widget(Label(text='Mobile App Starter Kit', font_size=28))
        self.add_widget(Label(text='Python Kivy prototype screen'))
        self.add_widget(Button(text='Start'))


class StarterApp(App):
    def build(self):
        return Home()


if __name__ == '__main__':
    StarterApp().run()
