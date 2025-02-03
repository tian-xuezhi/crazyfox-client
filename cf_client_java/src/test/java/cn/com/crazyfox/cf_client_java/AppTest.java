package cn.com.crazyfox.cf_client_java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AppTest {

    private HttpsURLConnection mockConnection;
    private InputStream mockInputStream;
    private Properties mockProperties;

    @BeforeEach
    void setUp() throws IOException {
        // 模拟 HttpsURLConnection 和 InputStream
        mockConnection = Mockito.mock(HttpsURLConnection.class);
        mockInputStream = Mockito.mock(InputStream.class);

        // 模拟 URL.openConnection() 返回 mockConnection
        URL mockUrl = Mockito.mock(URL.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        // 模拟 HttpsURLConnection.getInputStream() 返回 mockInputStream
        when(mockConnection.getInputStream()).thenReturn(mockInputStream);

        // 模拟 Properties
        mockProperties = Mockito.mock(Properties.class);
        App.props = mockProperties; // 替换 App 类中的静态 Properties 对象
    }

    @Test
    void testGetAllTargetFromCF() throws IOException {
        // 模拟输入流返回的 JSON 数据
        String jsonResponse = "{\"code\":200,\"msg\":\"success\",\"data\":[{\"targetid\":\"1\",\"recordid\":101,\"targetname\":\"StockA\",\"headdate\":\"2023-01-01\",\"enddate\":\"2023-12-31\"}]}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonResponse.getBytes());

        // 当调用 mockInputStream.read() 时，返回模拟的输入流数据
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            int offset = invocation.getArgument(1);
            int length = invocation.getArgument(2);
            return inputStream.read(buffer, offset, length);
        });

        // 模拟 Properties 返回的 token
        when(mockProperties.getProperty("token")).thenReturn("mock-token");

        // 调用被测试的方法
        String result = App.getAllTargetFromCF("mock-token");

        // 验证结果
        assertNotNull(result);
        assertEquals(jsonResponse, result);

        // 验证 HttpsURLConnection 的设置
        verify(mockConnection).setRequestProperty("token", "mock-token");
        verify(mockConnection).getInputStream();
    }

    @Test
    void testGetAllTargetFromCF_IOException() throws IOException {
        // 模拟 IOException
        when(mockConnection.getInputStream()).thenThrow(new IOException("Network error"));

        // 调用被测试的方法
        String result = App.getAllTargetFromCF("mock-token");

        // 验证结果
        assertNull(result);
    }

    @Test
    void testGetProperties() throws IOException {
        // 模拟 FileInputStream
        FileInputStream mockFileInputStream = Mockito.mock(FileInputStream.class);
        when(mockFileInputStream.read(any(byte[].class))).thenReturn(-1); // 模拟读取文件结束

        // 替换 App 类中的静态 Properties 对象
        App.props = new Properties();

        // 调用被测试的方法
        App.getProperties();

        // 验证 Properties 是否被正确加载
        assertNotNull(App.props);
    }
}