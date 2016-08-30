# ARNavigator tools
## Comment
The following tools are only developed for Linux OS.
## ContentCreator
The ContentCreator is a GUI tool, which helps you with generating the content for a ARNavigator QR-Code. The content will be exported as a valid XML.
### How to install
Install ruby: https://rvm.io/rvm/install

Install dependencies:
```
sudo apt install libfox-1.6-dev
sudo apt install libxrandr-dev
gem install fxruby
```

### Usage
```
./content_creator.rb
```

## QRCreator
The QRCreator is a console application, which searches for ARNavigator XML files and converts them to a QR_CODE/qr_*name*.png and QR_CODE_PDF/qr_*name*.pdf in the given directory.
### How to install
```
gem install rqrcode
gem install prawn
```

### Usage
```
./qr_code_creator.rb dir/to/content/xmls
```

