package vul.Shiro;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class MainRealm extends AuthorizingRealm {

    // 用于授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // 获取当前授权的用户
        return null;
    }

    // 用于认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 获取当前用户身份 getPrincipal
        String username = (String) authenticationToken.getPrincipal();
        // 获取当前用户信用凭证 （其实就是获取密码 密码是 char类型的所以要转一下
        String password = new String((char[]) authenticationToken.getCredentials());
        if (username.equals("admin") && password.equals("admin")) {
            return new SimpleAuthenticationInfo((Object)username, (Object)password, this.getName());
        }
        throw new IncorrectCredentialsException("username or password  is incorrect.");
    }
}
