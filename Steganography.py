from PIL import Image
from bitarray import bitarray
            
def imageChoose():
            imagePath = input("What is the image's path? ")
            # Ask if they would like to preview image
            while True:
                previewChoice = input("Would you like to preview the image? ")
                if previewChoice.lower() == "yes":
                        previewImage = Image.open(imagePath)
                        previewImage.show()
                        
                        while True:
                            # Ask if that was image they meant
                            imageConfirm = input("Was that the image you wanted? ")
                            if imageConfirm.lower() == "yes":
                                return imagePath
                            elif imageConfirm.lower() == "no":
                                imageChoose()
                elif previewChoice.lower() == "no":
                       return imagePath
            return(imagePath)
    
def encode():
            # Ask for input image path
            inputPath = imageChoose()
            # Ask for output image path
            outputPath = input("What will be the hidden image's path? ")
            
            # Ask for message
            message = input("Input message to encode in image: ")
            
            # Open image specified by path
            img = Image.open(inputPath)
            # Allocates storage for the image and loads pixel data
            pixels = img.load()
            
            # Create bitarray object to store binary representation of message
            binaryMessage = bitarray()
            # Convert message into bytes and bytes into a sequence of binary bits
            binaryMessage.frombytes(message.encode("utf-8"))
            # Append special binary sequence to end of message so decoder knows when it ends
            binaryMessage.extend("1111111111111110")
            
            # Check if binary bits in this message is larger than the maximum bits the image can store
            if len(binaryMessage) > img.width * img.height * 3:
                raise ValueError("Message is too big to hide in this image.")

            # Encode each bit of message into the image
            dataIndex = 0
            # Loop through each row of pixels in the image
            for y in range(img.height):
                # Loop through each column of pixels in the row
                for x in range(img.width):
                    # Check if there any bits left to encode
                    if dataIndex < len(binaryMessage):
                        # Get the RGB values of the current pixel and convert it to pixels to be modified
                        pixel = list(pixels[x, y])
                        # Loop through the 3 channels Red, Green and Blue
                        for i in range(3):
                            if dataIndex < len(binaryMessage):
                                # Clear LSB of colour value and insert current bit of message into LSB
                                pixel[i] = (pixel[i] & 0xFE) | binaryMessage[dataIndex]
                                # Move to next bit in message 
                                dataIndex += 1
                        # Convert modified pixel back into a tuple and save it in image
                        pixels[x, y] = tuple(pixel) 
                    else:
                        break
            
            # Save the modified image to file specified by
            img.save(outputPath)
            # Output success message
            print("Message encoded successfully in", outputPath)
            
def decode():
            # Ask for output image path
            outputPath = imageChoose()

            # Open image specified by path
            img = Image.open(outputPath)
            # Allocates storage for the image and loads pixel data
            pixels = img.load()
            
            # Create bitarray object to store binary representation of message
            binaryMessage = bitarray()
            # Loop through each row of pixels in the image
            for y in range(img.height):
                # Loop through each column of pixels in the row
                for x in range(img.width):
                    # Get the RGB values of the current pixel and convert it to pixels to be modified
                    pixel = pixels[x, y]
                    # Loop through the 3 channels Red, Green and Blue
                    for i in range(3):
                        # Get RGB values of current pixel and extract the LSB of the colour value
                        binaryMessage.append(pixel[i] & 1)  
                        # Check if we have reached the end of message binary sequence
                        if binaryMessage[-16:] == bitarray('1111111111111110'):
                            # Remove last 16 binary bits of message that contain the special binary sequence 
                            binaryMessage = binaryMessage[:-16]
    
                            try:
                                # Convert remaining binary data back to string
                                return binaryMessage.tobytes().decode('utf-8')
                            # If byte sequence is not valid UTF-8 raise error instead of crashing
                            except UnicodeDecodeError:
                                return "Decoded data is not a valid UTF-8 string so image likely contains no message."                        
            # If special binary sequence isn't found output unsuccessful message
            return("No message found")
            
# Main menu function
def mainmenu():
    # Keep asking options until a valid one is entered
    while True:
        options = input("Would you like to encode or decode an image or quit the application? ")
        if options.lower() == "encode":
            encode()
        elif options.lower() == "decode":
            decodeResult = decode()
            print("Hidden message:", decodeResult)
        # If user enters quit end while loop
        elif options.lower() == "quit":
            break
    quit()
    
print("Welcome to Georgia's Steganography tool. Please use yes or no to answer questions rather than y, n , yeah etc.")
mainmenu()
