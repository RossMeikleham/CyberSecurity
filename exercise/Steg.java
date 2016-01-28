import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

class Steg
{



/**
 * A constant to hold the number of bits per byte
 */
private final int byteLength=8;

/**
 * A constant to hold the number of bits used to store the size of the file extracted
 */
protected final int sizeBitsLength=32;
/**
 * A constant to hold the number of bits used to store the extension of the file extracted
 */
protected final int extBitsLength=64;

// Constant containing the number of header bytes in a bmp image
protected final int headerByteSize=54;


protected final String failMsg = "Fail";

 /**
Default constructor to create a steg object, doesn't do anything - so we actually don't need to declare it explicitly. Oh well. 
*/

public Steg()
{

}

/**
A method for hiding a string in an uncompressed image file such as a .bmp or .png
You can assume a .bmp will be used
@param cover_filename - the filename of the cover image as a string 
@param payload - the string which should be hidden in the cover image.
@return a string which either contains 'Fail' or the name of the stego image which has been 
written out as a result of the successful hiding operation. 
You can assume that the images are all in the same directory as the java files
*/
//TODO you must write this method
public String hideString(String payload, String cover_filename) {
    byte[] fileBytes;
    byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

    Optional<byte[]> optFileBytes =  readBMPFile(cover_filename);
    if (!optFileBytes.isPresent()) {
        return failMsg;
    }

    fileBytes = optFileBytes.get();

    // Check Header
    if (fileBytes.length < headerByteSize) {
        System.err.println("Malformed bmp filed received, doesn't contain proper header");
        return failMsg;
    }

    // Check we have enough space in the file to hide the payload
    if ((payloadBytes.length * byteLength) > fileBytes.length - headerByteSize) {
        System.err.println("Not enough space to hide the payload");
        return failMsg;
    }

    int currentByteNo = headerByteSize;
    // Modify LSBs to include payload
    for (int i = 0; i < payloadBytes.length; i++) {
        byte payloadByte = payloadBytes[i];

        for (int j = 0; j < byteLength; j++, currentByteNo++) {
            int payloadBit = (payloadByte & (0x1 << j)) > 0 ? 0x1 : 0x0;
            fileBytes[currentByteNo] = (byte)swapLsb(payloadBit, fileBytes[currentByteNo]);
        }
    }

    String outFileName = cover_filename.substring(0, cover_filename.length() - 4) + "_steg.bmp";

    try {
        FileOutputStream output = new FileOutputStream (new File(outFileName));
        output.write(fileBytes);
        output.close();
    } catch(IOException e) {
        System.err.println("Failed to write modified file");
        return failMsg;
    }

    return outFileName;
}

//TODO you must write this method
/**
The extractString method should extract a string which has been hidden in the stegoimage
@param the name of the stego image 
@return a string which contains either the message which has been extracted or 'Fail' which indicates the extraction
was unsuccessful
*/
public String extractString(String stego_image)
{
    byte[] fileBytes;

    Optional<byte[]> optFileBytes =  readBMPFile(stego_image);
    if (!optFileBytes.isPresent()) {
        return failMsg;
    }

    fileBytes = optFileBytes.get();

    // Check Header
    if (fileBytes.length < headerByteSize) {
        System.err.println("Malformed bmp filed received, doesn't contain proper header");
        return failMsg;
    }


    int currentByteNo = headerByteSize;
    byte[] messageBytes = new byte[100];

    // Checks LSBs for payload
    for (int i = 0; i < 100; i++) {
        byte currentMessageByte = 0x0;

        for (int j = 0; j < byteLength; j++, currentByteNo++) {
            int currentMessageBit = fileBytes[currentByteNo] & 0x1;
            currentMessageByte |= (currentMessageBit << j);
        }

        messageBytes[i] = currentMessageByte;
    }

    return new String(messageBytes, StandardCharsets.UTF_8);
}


private Optional<byte[]> readFile(String fileName) {
    byte[] fileBytes;

    try {
        fileBytes = Files.readAllBytes(new File(fileName).toPath());
    } catch(IOException e) {
        System.err.println("Failed to read file");
        return Optional.empty();
    }

    return Optional.of(fileBytes);
 
}

/* Attempts to read in all bytes from a given uncompressed bitmap file,
   returns an Optional value containing the bytes of the file if succesful,
   otherwise returns an empty optional value on failure */
private Optional<byte[]> readBMPFile(String fileName) {

    int index = fileName.lastIndexOf('.');
    if (!(index >= 0 && fileName.substring(index).equals(".bmp"))) {
        System.err.println("Expected file with bmp extension");
        return Optional.empty();
    }

    return readFile(fileName);
}


//TODO you must write this method
/**
The hideFile method hides any file (so long as there's enough capacity in the image file) in a cover image

@param file_payload - the name of the file to be hidden, you can assume it is in the same directory as the program
@param cover_image - the name of the cover image file, you can assume it is in the same directory as the program
@return String - either 'Fail' to indicate an error in the hiding process, or the name of the stego image written out as a
result of the successful hiding process
*/
public String hideFile(String file_payload, String cover_image)
{
    byte[] fileBytes;

    Optional<byte[]> optFileBytes =  readBMPFile(cover_image);
    if (!optFileBytes.isPresent()) {
        return failMsg;
    }

    fileBytes = optFileBytes.get();

    // Check Header
    if (fileBytes.length < headerByteSize) {
        System.err.println("Malformed bmp filed received, doesn't contain proper header");
        return failMsg;
    }
	
    // Setup the FileReader for the Payload File
    FileReader payloadReader = new FileReader(file_payload);
    if (!payloadReader.getSuccessBool()) {
        return failMsg;
    } 
 
    // Check we have enough space to store the payload with the file
    // extension + payload bits   
    if (fileBytes.length < headerByteSize + sizeBitsLength + 
            extBitsLength + payloadReader.getFileSize()) {
        System.err.println("Not enough space in the bmp to store the file");
        return failMsg;
    }

    int currentFileByte = headerByteSize;

    // Write Size Bits
    for (int i = 0; i < sizeBitsLength; i++) {
        if (!payloadReader.hasNextBit()) {
            System.err.println("Unable to obtain size bits in payload");
            return failMsg;
        }
         
        fileBytes[currentFileByte] = (byte)swapLsb(payloadReader.getNextBit(), 
                                        fileBytes[currentFileByte]);
        currentFileByte++;
    }
    
    // Write Extension Bits
    for (int i = 0; i < extBitsLength; i++) {
        if (!payloadReader.hasNextBit()) {
            System.err.println("Unable to obtain extension bits in payload");
            return failMsg;
        }
        int bit = payloadReader.getNextBit();
        fileBytes[currentFileByte] = (byte)swapLsb(bit, fileBytes[currentFileByte]);
        currentFileByte++; 
    } 

    // Write File Payload
    while (payloadReader.hasNextBit()) {
        Integer bit = payloadReader.getNextBit();
        fileBytes[currentFileByte] = (byte)swapLsb(bit, fileBytes[currentFileByte]);
        currentFileByte++;
    }   

    // Check if anything went wrong
    if (!payloadReader.getSuccessBool()) {
        System.err.println("Something went wrong");
        return failMsg;
    } 
    
    
    String outFileName = cover_image.substring(0, cover_image.length() - 4) + "_steg.bmp";
    
    try {
        FileOutputStream output = new FileOutputStream (new File(outFileName));
        output.write(fileBytes);
        output.close();
    } catch(IOException e) {
        System.err.println("Failed to write modified file");
        return failMsg;
    }

    return outFileName;
}

//TODO you must write this method
/**
The extractFile method hides any file (so long as there's enough capacity in the image file) in a cover image

@param stego_image - the name of the file to be hidden, you can assume it is in the same directory as the program
@return String - either 'Fail' to indicate an error in the extraction process, or the name of the file written out as a
result of the successful extraction process
*/
public String extractFile(String stego_image) {
    byte[] fileBytes;

    Optional<byte[]> optFileBytes =  readBMPFile(stego_image);
    if (!optFileBytes.isPresent()) {
        return failMsg;
    }

    fileBytes = optFileBytes.get();

    // Check Header
    if (fileBytes.length < headerByteSize) {
        System.err.println("Malformed bmp filed received, doesn't contain proper header");
        return failMsg;
    }

    if (fileBytes.length < headerByteSize + sizeBitsLength + extBitsLength) {
        System.err.println("BMP doesn't contain enough space to store the payload metadata");
        return failMsg;
    } 

    // Get File Size
    int currentFileByte = headerByteSize;
    int payloadFileSize = 0;
    for (int i = 0; i < sizeBitsLength; i++) {
        payloadFileSize |= ((0x1 & fileBytes[currentFileByte]) << i);
        currentFileByte++;
    }
    
	// Get File Extension
    byte[] bExtension = new byte[extBitsLength / byteLength];
    for (int i = 0; i < extBitsLength / byteLength; i++) {
        bExtension[i] = 0x0;
        for (int j = 0; j < byteLength; j++) {
            bExtension[i] |= ((0x1 & fileBytes[currentFileByte]) << j);
            currentFileByte++;
        }
    } 
    String payloadExtension = new String(bExtension, StandardCharsets.UTF_8).trim();

    // Get Payload
    byte payloadFile[] = new byte[payloadFileSize / byteLength]; 
    for (int i = 0; i <  payloadFileSize / byteLength; i++) {
        
        payloadFile[i] = 0x0;
        for (int bit = 0; bit < byteLength; bit++) {
            payloadFile[i] |= (fileBytes[currentFileByte] & 0x1) << bit;
            currentFileByte++;
        }
    }

    String temp = stego_image.substring(0, stego_image.length() - 4) + "_file";
    String outFileName = temp + payloadExtension;
    
    try {
        FileOutputStream output = new FileOutputStream (new File(outFileName));
        output.write(payloadFile);
        output.close();
    } catch(IOException e) {
        System.err.println(outFileName);
        System.err.println("Failed to retrieve embedded file" + e);
        return failMsg;
    }

    return outFileName;
}

//TODO you must write this method
/**
 * This method swaps the least significant bit with a bit from the filereader
 * @param bitToHide - the bit which is to replace the lsb of the byte of the image
 * @param byt - the current byte
 * @return the altered byte
 */
public int swapLsb(int bitToHide,int byt) {
    return (byt & (~0x0 - 0x1)) | bitToHide;
}




}
