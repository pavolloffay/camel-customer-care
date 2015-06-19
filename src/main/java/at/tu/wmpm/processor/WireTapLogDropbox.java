package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.dropbox.util.DropboxResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WireTapLogDropbox implements Processor {

    private static final Logger log = LoggerFactory
            .getLogger(WireTapLogDropbox.class);

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Dropbox Log");

        Map<String, Object> headers = exchange.getIn().getHeaders();
        Object bodyRaw = exchange.getIn().getBody();
        String body = "";
        String bodyTemp = "";
        Map<String, DropboxResultCode> bodyMap = null;
        String uploadedFile = "";
        String uploadedFiles = "";

        if (null != headers) {
            if (headers.containsKey("UPLOADED_FILE")) {
                uploadedFile = (String) headers.get("UPLOADED_FILE");
                body = "UPLOADED_FILE (remote): " + uploadedFile;
            } else
                uploadedFile = "No uploaded file String";

            if (headers.containsKey("UPLOADED_FILES")) {
                uploadedFiles = (String) headers.get("UPLOADED_FILES");
                body = "UPLOADED_FILES (remote): " + uploadedFiles;
            } else
                uploadedFiles = "No uploaded files String";
        }

        if (null != bodyRaw) {
            if (bodyRaw instanceof String)
                bodyTemp = exchange.getIn().getBody(String.class);
            else
                bodyTemp = "No body String";

            if (bodyRaw instanceof Map<?, ?>)
                bodyMap = (Map<String, DropboxResultCode>) exchange.getIn()
                        .getBody();
            else {
                bodyMap = null;
            }
        }

        if (null != bodyMap) {
            for (Map.Entry<String, DropboxResultCode> entry : bodyMap
                    .entrySet()) {
                bodyTemp = bodyTemp + "\n" + "(" + entry.getKey() + " / "
                        + entry.getValue() + ")";
            }
        }

        body = body + "\nBODY: " + bodyTemp;
        log.info("{}", body);

        exchange.getIn().setBody(body);
    }
}
