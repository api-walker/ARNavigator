#!/usr/bin/env ruby

require 'rqrcode'
require 'prawn'
require 'prawn/measurement_extensions'
require 'fileutils'

# Size of QR-Code
QR_CODE_PDF_SIZE_RAW = 8 #cm
QR_CODE_PDF_SIZE = QR_CODE_PDF_SIZE_RAW.cm
QR_CODE_PDF_LOGO = "res/arnavigator_ready_logo.png"
QR_CODE_PDF_LOGO_HEIGHT = (QR_CODE_PDF_SIZE_RAW.to_f/4).cm + 0.15.cm

# Extract working directory from cmd
if ARGV.size == 0
  puts "Usage: #{$0} dir/to/content/xmls"
  exit
end

# Set working dir
dir = ARGV.first
if Dir.exist? dir
  # Get all xml files in working directory
  xml_files = Dir["#{dir}/*.xml"]
  # Set save folders
  qr_code_folder =File.join(dir, "QR-Code")
  qr_code_folder_pdf = File.join(dir, "QR-Code PDF")

  # Remove existing QR-Codes
  FileUtils.rm_rf(qr_code_folder)
  FileUtils.rm_rf(qr_code_folder_pdf)

  # Create QR-Code folders
  FileUtils.mkdir_p(qr_code_folder)
  FileUtils.mkdir_p(qr_code_folder_pdf)

  files_count = xml_files.size

  xml_files.each_with_index do |file, i|
    # Generate QR-Code
    qrcode = RQRCode::QRCode.new(File.read(file), :level => :m)
    original_xml_file = File.basename(file)

    new_png_path = File.join(qr_code_folder, "qr_#{File.basename(file, File.extname(file))}.png")
    new_pdf_path = File.join(qr_code_folder_pdf, "qr_#{File.basename(file, File.extname(file))}.pdf")

    # Save as PNG
    qrcode.as_png(file: new_png_path)
    puts "(#{i+1}/#{files_count}) Generated PNG: #{new_png_path}"

    # Generate PDF
    Prawn::Document.generate(new_pdf_path) do
      text "File: #{original_xml_file}"
      text "At: #{Time.now.strftime("%T %d.%m.%Y")}"

      bounding_box([bounds.left, cursor], :width => QR_CODE_PDF_SIZE, :height => QR_CODE_PDF_SIZE + QR_CODE_PDF_LOGO_HEIGHT) do
        image QR_CODE_PDF_LOGO, :width => QR_CODE_PDF_SIZE, :scale => true
        image new_png_path, :width => QR_CODE_PDF_SIZE, :height => QR_CODE_PDF_SIZE

        # Stroke the QR Code
        stroke_bounds
      end
    end
    # Set pdf state
    puts "(#{i+1}/#{files_count}) Generated PDF: #{new_pdf_path}"
  end
else
    puts "Folder not found!"
end