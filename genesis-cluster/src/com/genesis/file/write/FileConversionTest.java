package com.genesis.file.write;

import com.google.protobuf.ByteString;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * 
 * @author navdeepdahiya
 *
 */
@FixMethodOrder
public class FileConversionTest {

	private boolean keepFile = false;
	private List<ByteString> list;
	private FileConversion conversionObj;

	@Before
	public void setup() {
		conversionObj = new FileConversion();
		list = new LinkedList<>();
	}
/*
	@Test
	public void testReadfile() {
		list = instance.readAndConvert("src/com/genesis/file/write/dump.jpg", 0, 1000000, -1);
		assertEquals(list.get(0).size(), 1000000);

	}
*/
	@Test
	public void testWriteFile() {
		
		list = conversionObj.readAndConvert("src/com/genesis/file/write/Java_Programming_with_BlueJ.pdf");

		System.out.println(list.size());
		
		conversionObj.convertAndWrite("src/com/genesis/file/write/output/Java_Programming_with_BlueJ.pdf", list);
		File file = new File("src/com/genesis/file/write/output/Java_Programming_with_BlueJ.pdf");
		
		assertTrue(file.exists());
		assertTrue(file.isFile());
		//assertEquals(8388608, file.length());
	}

	@After
	public void tearDown() {
		
	}
}
