package vnpay.com.payment_with_vn_pay.cotroller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vnpay.com.payment_with_vn_pay.config.Config;
import vnpay.com.payment_with_vn_pay.dto.ResultDto;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/vnpay_jsp")
public class PaymentController {

    @GetMapping("/create_payment")
    protected ResponseEntity<?> doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = 10000*100;
        String bankCode = req.getParameter("bankCode");

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = Config.getIpAddress(req);

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = req.getParameter("language");
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
        ResultDto resultDto = new ResultDto();
        resultDto.setUrl(paymentUrl);
        return ResponseEntity.ok(resultDto);
    }

    @GetMapping("/vnpay_return.jsp")
    public ResponseEntity<?> transaction(
            @RequestParam(value = "vnp_Amount") String vnp_Amount,
            @RequestParam(value = "vnp_BankCode") String vnp_BankCode,
            @RequestParam(value = "vnp_BankTranNo") String vnp_BankTranNo,
            @RequestParam(value = "vnp_CardType") String vnp_CardType,
            @RequestParam(value = "vnp_PayDate")String vnp_PayDate,
            @RequestParam(value = "vnp_ResponseCode") String vnp_ResponseCode,
            @RequestParam(value = "vnp_TransactionNo") String vnp_TransactionNo,
            @RequestParam(value = "vnp_TransactionStatus") String vnp_TransactionStatus,
            @RequestParam(value = "vnp_TxnRef") String vnp_TxnRef,
            @RequestParam(value = "vnp_SecureHash") String vnp_SecureHash

    ){
        if(vnp_ResponseCode.equals("00")){
            ResultDto resultDto = new ResultDto();
            resultDto.setUrl("thanh cong");
            return ResponseEntity.ok(resultDto);
        }
        ResultDto resultDto = new ResultDto();
        resultDto.setUrl("that bai");
        return ResponseEntity.ok(resultDto);

    }

}
