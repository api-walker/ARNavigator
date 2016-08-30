#!/usr/bin/env ruby

require 'fox16'
require 'nokogiri'

include Fox

class ContentCreatorWindow < FXMainWindow

  def initialize(app)
    @app = app
    # Initialize base class first
    super(app, "ARNavigator ContentCreator", :opts => DECOR_ALL, :width => 500, :height => 400)

    # Text area plus a button
    vertical_frame = FXVerticalFrame.new(self, LAYOUT_FILL)
    FXLabel.new(vertical_frame, "Type:")
    # Add type combobox
    @type_combobox = FXComboBox.new(vertical_frame, 20, nil, 0, FRAME_SUNKEN|LAYOUT_FILL_X)
    @type_combobox.appendItem("ROOM")
    @type_combobox.appendItem("MAP")
    @type_combobox.appendItem("MEDIA")
    @type_combobox.appendItem("WEB_CONTENT")
    @type_combobox.appendItem("ONLINE_TARGET")
    @type_combobox.appendItem("EXIT")
    @type_combobox.appendItem("STAIRS_UP")
    @type_combobox.appendItem("STAIRS_DOWN")
    @type_combobox.appendItem("ADJUSTMENT_POINT")
    @type_combobox.numVisible = 5

    #Add name
    FXLabel.new(vertical_frame, "Name:")
    @txt_name = FXTextField.new(vertical_frame, 30, :opts => FRAME_SUNKEN|FRAME_THICK|LAYOUT_FILL_X)
    @txt_name.text = "Labor"

    #Add ID
    FXLabel.new(vertical_frame, "ID:")
    @txt_id = FXTextField.new(vertical_frame, 30, :opts => FRAME_SUNKEN|FRAME_THICK|LAYOUT_FILL_X)
    @txt_id.text = "1"

    #Add content
    FXLabel.new(vertical_frame, "Content:")
    @cb_raw = FXCheckButton.new(vertical_frame, "Is raw content?")
    @txt_content = FXText.new(vertical_frame, nil, 0, :opts => FRAME_SUNKEN|FRAME_THICK|LAYOUT_FILL)
    @txt_content.text = "Content"

    #Add save button
    @btn_save = FXButton.new(vertical_frame, "Save as XML")
    @btn_save.connect(SEL_COMMAND) do
      destination = FXFileDialog.getSaveFilename(self, "Save to...", "./", "*.xml")
      # Append .xml if not existing
      destination += ".xml" unless File.extname(destination) == ".xml"
      save_to_xml self, destination
    end
  end

  # Create and show the main window
  def create
    super
    show(PLACEMENT_SCREEN)
  end

  private
  def save_to_xml(owner, file)
    # Build XML
    xml = Nokogiri::XML::Builder.new { |xml|
      xml.QRContent do
        xml.Meta do
          xml.type @type_combobox.text
          xml.name @txt_name.text
          xml.id @txt_id
          xml.content @txt_content.text, :raw => @cb_raw.check
        end
      end
    }.to_xml

    # Save to file
    File.open(file, "w") do |fd|
      fd.write xml
    end
    FXMessageBox.information(owner, MBOX_OK, "Saved successfully", "Written to: #{file}")
  end
end

if $0 == __FILE__
  # Construct an application
  application = FXApp.new('ContentCreator', 'ARNavigator')

  # Construct the main window
  ContentCreatorWindow.new(application)

  # Create and show the application windows
  application.create

  # Run the application
  application.run
end
 
