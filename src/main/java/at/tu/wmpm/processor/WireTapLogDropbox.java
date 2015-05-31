package at.tu.wmpm.processor;

//import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
//import org.apache.camel.component.dropbox.util.DropboxResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WireTapLogDropbox implements Processor {

    private static final Logger log = LoggerFactory
            .getLogger(WireTapLogDropbox.class);

//    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Dropbox Log");

//        Map<String, Object> headers = exchange.getIn().getHeaders();
//        Object bodyRaw = exchange.getIn().getBody();
//        String body = "";
//        Map<String, DropboxResultCode> bodyMap = null;
//        String uploadedFile = "";
//        String uploadedFiles = "";
//
//        if (null != headers) {
//            if (headers.containsKey("UPLOADED_FILE"))
//                uploadedFile = (String) headers.get("UPLOADED_FILE");
//            else
//                uploadedFile = "No uploaded file String";
//
//            if (headers.containsKey("UPLOADED_FILES"))
//                uploadedFiles = (String) headers.get("UPLOADED_FILES");
//            else
//                uploadedFiles = "No uploaded files String";
//        }
//
//        if (null != bodyRaw) {
//            if (bodyRaw instanceof String)
//                body = exchange.getIn().getBody(String.class);
//            else
//                body = "No body String";
//
//            if (bodyRaw instanceof Map<?, ?>)
//                bodyMap = (Map<String, DropboxResultCode>) exchange.getIn()
//                        .getBody();
//            else {
//                bodyMap = null;
//            }
//        }
//
//        log.info("UPLOADED_FILE: ?", uploadedFile);
//        log.info("UPLOADED_FILES: ?", uploadedFiles);
//        log.info("BODY: ?", body);
//        log.info("BODY_MAP:");
//        if (null != bodyMap) {
//            for (Map.Entry<String, DropboxResultCode> entry : bodyMap
//                    .entrySet()) {
//                log.info("? / ?", entry.getKey(), entry.getValue());
//            }
//        } else
//            log.info("No body map");
    }
}