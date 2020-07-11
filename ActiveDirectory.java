import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class ActiveDirectory {

    public static void main(String... args) {
        if (args.length != 2) {
            System.out.println("아이디와 암호을 입력해주세요.");
            return;
        }
        String id = args[0];
        String pw = args[1];
        System.out.println("아이디 / 암호 : " + id + " / " + pw);

        System.out.println(login(id, pw));
    }

    // 아래에는 해당하는 정보를 넣어주세요.
    static String url = "ldap://IP주소:389";
    static String baseDn = "OU=Person,DC=test,DC=local";
    static String domain = "@test.local";

    public static LoginResult login(String id, String pw) {

        try {
            try{
                Hashtable<String, String> env = new Hashtable<>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, url);
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, id + domain);
                env.put(Context.SECURITY_CREDENTIALS, pw);

                LdapContext ctx = new InitialLdapContext(env, null);

                SearchControls sc = new SearchControls();
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

                NamingEnumeration<SearchResult> answer = ctx.search(baseDn, "sAMAccountName=" + id, sc);

                while (answer.hasMoreElements()) {
                    return new LoginResult(true, "성공");
                }
            } catch(AuthenticationException e) {

                String msg = e.getMessage();
                if (msg.indexOf("data 525") > 0) {
                    return new LoginResult(false, "존재하지 않는 ID입니다.");
                } else if (msg.indexOf("data 773") > 0) {
                    return new LoginResult(false, "암호를 재설정해야합니다.");
                } else if (msg.indexOf("data 52e") > 0) {
                    return new LoginResult(false, "ID와 암호가 일치하지 않습니다.");
                } else if (msg.indexOf("data 533") > 0) {
                    return new LoginResult(false, "입력한 ID는 비활성화 상태 입니다.");
                } else if(msg.indexOf("data 532") > 0){
                    return new LoginResult(false, "암호가 만료되었습니다.");
                } else if(msg.indexOf("data 701") > 0){
                    return new LoginResult(false, "ID가 만료되었습니다.");
                } else {
                    e.printStackTrace();
                    return new LoginResult(false, "인증오류:\n" + e.getMessage());
                }
            }
        } catch(Exception jex) {
            jex.printStackTrace();
            return new LoginResult(false, "자바오류:\n" + jex.getMessage());
        }

        return new LoginResult(false, "계정정보를 찾을 수 없습니다.");
    }

    public static class LoginResult {
        final private boolean success;
        final private String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "LoginResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
