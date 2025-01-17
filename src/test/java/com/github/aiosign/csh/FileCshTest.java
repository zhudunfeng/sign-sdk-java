package com.github.aiosign.csh;

import com.alibaba.fastjson.JSON;
import com.github.aiosign.AbstractSignTest;
import com.github.aiosign.base.FileItem;
import com.github.aiosign.enums.ContentType;
import com.github.aiosign.enums.HttpMethod;
import com.github.aiosign.module.request.CommonRequest;
import com.github.aiosign.module.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 城商行定制
 * <p>
 * 均使用原始调用Api，除上传以及下载文件，实体类方式后期不再维护
 * requestBody请求参数:可序列化对象,具体请求参数，请参考Api文档中调用接口请求参数
 * reponse响应参数:Object,具体响应参数，请参考Api文档中调用接口响应参数
 *
 * @author Zhu Dunfeng
 * @date 2023/5/29
 */
@Slf4j
public class FileCshTest extends AbstractSignTest {

    /**
     * 上传合同
     */
    @Test
    public void uploadContract() {
        String fileName = "测试合同";
        String fileType = "contract";
        String userId = "10144942136450173440";
        // FileItem fileItem = new FileItem(new File("C:\\Users\\Administrator\\Desktop\\房屋合同.pdf"));
        FileItem fileItem = new FileItem(new File("E:\\worktwo\\csh\\三方签署测试pdf\\bd6ad2b269b6bc4babdef71d95ae6037.pdf"));

        CommonRequest request = new CommonRequest();
        request.setApiUri("/v1/file/upload");// 请求Api地址
        request.setNeedToken(true);// 是否需要token
        request.setContentType(ContentType.MULTIPART);// 请求头类型
        request.setMethod(HttpMethod.POST);// 请求方法
        Map<String, FileItem> map = new HashMap<>(2);
        map.put("file", fileItem);
        request.setFileParams(map);
        Map<String, String> params = request.getParams();
        params.put("file_name", fileName);
        params.put("file_type", fileType);
        params.put("user_id", userId);
        CommonResponse execute = signClient.execute(request);
        log.info("响应状态：{}", execute.getResultCode());
        log.info("响应信息：{}", execute.getResultMessage());
        log.info("响应数据：{}", JSON.toJSONString(execute.getData()));
    }

    /**
     * 上传模板
     */
    @Test
    public void uploadTemplate() {
        String fileName = "测试模板";
        String fileType = "template";
        FileItem fileItem = new FileItem(new File("C:\\Users\\Administrator\\Desktop\\template.pdf"));

        CommonRequest request = new CommonRequest();
        request.setApiUri("/v1/file/upload");// 请求Api地址
        request.setNeedToken(true);// 是否需要token
        request.setContentType(ContentType.MULTIPART);// 请求头类型
        request.setMethod(HttpMethod.POST);// 请求方法
        Map<String, FileItem> map = new HashMap<>(2);
        map.put("file", fileItem);
        request.setFileParams(map);
        Map<String, String> params = request.getParams();
        params.put("file_name", fileName);
        params.put("file_type", fileType);
        CommonResponse execute = signClient.execute(request);
        log.info("响应状态：{}", execute.getResultCode());
        log.info("响应信息：{}", execute.getResultMessage());
        log.info("响应数据：{}", JSON.toJSONString(execute.getData()));
    }

    /**
     * 下载文件
     *
     * @throws FileNotFoundException
     */
    @Test
    public void downloadFile() throws FileNotFoundException {
        String baseUri = "/v1/file/download";
        // String fileId = "ceafec7cdfaf4f3b8f7af21966a137e9";
        String fileId = "df5bbadf4986425f9e4018d80e73b9cc";
        FileOutputStream out = new FileOutputStream("contract.pdf");
        byte[] fileBytes = signClient.download(baseUri, fileId);
        try {
            out.write(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载文件报告
     *
     * @throws FileNotFoundException
     */
    @Test
    public void downloadReport() throws FileNotFoundException {
        String baseUri = "/v1/file/downloadReport";
        String fileId = "43a53ed66ca34361821d2ab137e02824";
        FileOutputStream out = new FileOutputStream("E:\\worktwo\\csh\\三方签署测试pdf\\report.pdf");
        byte[] fileBytes = signClient.download(baseUri, fileId);
        try {
            out.write(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 上传合同文件下载报告
     */
    @Test
    public void uploadAndDownloadReportFileStream() throws FileNotFoundException {
        //上传的合同文件
        FileItem fileItem = new FileItem(new File( "D:\\gd\\ideaprojects\\sign-sdk-java\\data\\contract.pdf"));
        CommonRequest request = new CommonRequest();
        request.setApiUri("/v1/file/downloadReportFile");// 请求Api地址
        request.setNeedToken(true);// 是否需要token
        request.setContentType(ContentType.MULTIPART);// 请求头类型
        request.setMethod(HttpMethod.POST);// 请求方法
        Map<String, FileItem> map = new HashMap<>(2);
        map.put("file", fileItem);
        request.setFileParams(map);
        Map<String, String> params = request.getParams();
        params.put("file_name", "测试合同1.pdf");
        params.put("file_type", "contract");
        params.put("user_id", "10150820344036936192");
        //下载的报告文件
        byte[] fileBytes = signClient.executeDownload(request);
        FileOutputStream out = new FileOutputStream("D:\\gd\\ideaprojects\\sign-sdk-java\\data\\report22.pdf");
        try {
            out.write(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
