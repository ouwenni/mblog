/*
+--------------------------------------------------------------------------
|   WeCMS [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.web.controller.site.posts;

import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.base.utils.FileKit;
import com.mtons.mblog.base.utils.MD5;
import com.mtons.mblog.modules.data.AccountProfile;
import com.mtons.mblog.modules.entity.Resource;
import com.mtons.mblog.modules.repository.ResourceRepository;
import com.mtons.mblog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;

/**
 * Ueditor 文件上传
 *
 * @author langhsu
 */
@Controller
@RequestMapping("/post")
public class UploadController extends BaseController {
    public static HashMap<String, String> errorInfo = new HashMap<>();

    static {
        errorInfo.put("SUCCESS", "SUCCESS"); //默认成功
        errorInfo.put("NOFILE", "未包含文件上传域");
        errorInfo.put("TYPE", "不允许的文件格式");
        errorInfo.put("SIZE", "文件大小超出限制，最大支持2Mb");
        errorInfo.put("ENTYPE", "请求类型ENTYPE错误");
        errorInfo.put("REQUEST", "上传请求异常");
        errorInfo.put("IO", "IO异常");
        errorInfo.put("DIR", "目录创建失败");
        errorInfo.put("UNKNOWN", "未知错误");
    }

    @PostMapping("/upload")
    @ResponseBody
    public UploadResult upload(@RequestParam(value = "file", required = false) MultipartFile file,
                               HttpServletRequest request, StandardMultipartHttpServletRequest standardMultipartHttpServletRequest) throws IOException, ServletException {
        UploadResult result = new UploadResult();

        file = standardMultipartHttpServletRequest.getFile("file_data");

        String crop = request.getParameter("crop");
        int size = ServletRequestUtils.getIntParameter(request, "size", siteOptions.getIntegerValue(Consts.STORAGE_MAX_WIDTH));

        // 检查空
        if (null == file || file.isEmpty()) {
            return result.error(errorInfo.get("NOFILE"));
        }

        String fileName = file.getOriginalFilename();

        // 检查类型
/*        if (!FileKit.checkFileType(fileName)) {
            return result.error(errorInfo.get("TYPE"));
        }*/

        // 检查大小
/*        String limitSize = siteOptions.getValue(Consts.STORAGE_LIMIT_SIZE);
        if (StringUtils.isBlank(limitSize)) {
            limitSize = "2";
        }
        if (file.getSize() > (Long.parseLong(limitSize) * 1024 * 1024)) {
            return result.error(errorInfo.get("SIZE"));
        }*/

        // 保存图片
        try {
            String path;
            /*if (StringUtils.isNotBlank(crop)) {
                Integer[] imageSize = siteOptions.getIntegerArrayValue(crop, Consts.SEPARATOR_X);
                int width = ServletRequestUtils.getIntParameter(request, "width", imageSize[0]);
                int height = ServletRequestUtils.getIntParameter(request, "height", imageSize[1]);
                path = storageFactory.get().storeScale(file, Consts.thumbnailPath, width, height);
            } else {
                path = storageFactory.get().storeScale(file, Consts.thumbnailPath, size);
            }*/

            //修改存储目录
            AccountProfile profile = getProfile();
            File filepath = new File("/storage/"+profile.getUsername());
            if(!filepath.exists()  && !filepath.isDirectory()){
                filepath.mkdirs();
            }
            path = storageFactory.get().store(file,"/storage/"+profile.getUsername());


            result.ok(errorInfo.get("SUCCESS"));
            result.setName(fileName);
            result.setPath(path);
            result.setSize(file.getSize());

        } catch (Exception e) {
            result.error(errorInfo.get("UNKNOWN"));
            e.printStackTrace();
        }

        return result;
    }

    @RequestMapping("/downloadFile")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response){
        String fileName = request.getParameter("filename");

        System.out.println(fileName);



        try {

            //mac系统，所以路径是这样子的。win系统就是D盘什么什么的

            String path = "./" + fileName;

            //这里是下载以后的文件叫做什么名字。我这里是以时间来定义名字的。

            response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", fileName));

            response.setContentType("application/octet-stream;charset=utf-8");

            response.setCharacterEncoding("UTF-8");

            OutputStream out;

            FileInputStream inputStream = new FileInputStream(path);

            out = response.getOutputStream();

            byte[] buffer = new byte[1024];

            int len;

            while ((len = inputStream.read(buffer)) != -1) {

                out.write(buffer, 0, len);

            }

            inputStream.close();

            out.close();

            out.flush();



        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    @Autowired
    ResourceRepository resourceRepository;

    @RequestMapping("/deleteFile")
    @ResponseBody
    public UploadResult deleteFile(@RequestParam(value = "key") String key) throws Exception{
        UploadResult result = new UploadResult();

        String filePath = "./"+key;
        byte[] bytes = fileToBytes(filePath);
        String md5 = MD5.md5File(bytes);
        Resource resource = resourceRepository.findByMd5(md5);
        resourceRepository.deleteById(resource.getId());
        File file = new File(filePath);
        file.delete();

        return result;
    }

    public static byte[] fileToBytes(String filePath) {
        byte[] buffer = null;
        File file = new File(filePath);

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int n;

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            buffer = bos.toByteArray();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally{
                try {
                    if(null!=fis){
                        fis.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return buffer;
    }

    public static class UploadResult {
        public static int OK = 200;
        public static int ERROR = 400;

        /**
         * 上传状态
         */
        private int status;

        /**
         * 提示文字
         */
        private String message;

        /**
         * 文件名
         */
        private String name;

        /**
         * 文件大小
         */
        private long size;

        /**
         * 文件存放路径
         */
        private String path;

        public UploadResult ok(String message) {
            this.status = OK;
            this.message = message;
            return this;
        }

        public UploadResult error(String message) {
            this.status = ERROR;
            this.message = message;
            return this;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
