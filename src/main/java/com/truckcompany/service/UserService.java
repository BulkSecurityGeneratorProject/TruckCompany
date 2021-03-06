package com.truckcompany.service;

import com.truckcompany.domain.Authority;
import com.truckcompany.domain.Company;
import com.truckcompany.domain.Template;
import com.truckcompany.domain.User;
import com.truckcompany.repository.*;
import com.truckcompany.security.AuthoritiesConstants;
import com.truckcompany.security.SecurityUtils;
import com.truckcompany.service.util.RandomUtil;
import com.truckcompany.service.util.UploadException;
import com.truckcompany.service.util.UploadUtil;
import com.truckcompany.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CompanyRepository companyRepository;

    @Inject
    private PersistentTokenRepository persistentTokenRepository;

    @Inject
    private AuthorityRepository authorityRepository;

    @Inject
    private MailService mailService;

    @Inject
    private TemplateService templateService;

    @Inject
    private CompanyService companyService;

    @Inject
    private RouteListRepository routeListRepository;

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                userRepository.save(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    public Optional<User> changeInitialPasswordForAdmin(String key, String password) {
        log.debug("Create new password via link from email for admin according activate key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                String encryptedPassword = passwordEncoder.encode(password);
                user.setPassword(encryptedPassword);
                user.setActivationKey(null);
                userRepository.save(user);
                return user;
            });

    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> {
                ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
                return user.getResetDate().isAfter(oneDayAgo);
            })
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                userRepository.save(user);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                userRepository.save(user);
                return user;
            });
    }

    public User createUser(String login, String password, String firstName, String lastName, String email,
                           String langKey) {

        User newUser = new User();
        Authority authority = authorityRepository.findOne(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(ManagedUserVM managedUserVM) {
        User user = new User();
        fillFieldsForUser(user, managedUserVM);

        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setActivated(true);
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    public User createEmployee(ManagedUserVM managedUserVM, User userAdmin, HttpServletRequest request) {
        User user = new User();
        fillFieldsForUser(user, managedUserVM);

        Company company = companyRepository.getOne(userAdmin.getCompany().getId());
        user.setCompany(company);

        user.setActivationKey(UUID.randomUUID().toString().replaceAll("-", EMPTY).substring(0,20));
        user.setActivated(true);
        userRepository.save(user);
        log.debug("Created Information for Employee: {}", user);

        String baseUrl = request.getScheme() + // "http"
            "://" +                                // "://"
            request.getServerName() +              // "myhost"
            ":" +                                  // ":"
            request.getServerPort() +              // "80"
            request.getContextPath();              // "/myContextPath" or "" if deployed in root context

        mailService.sendCreatePasswordForEmployeeEmail(user, baseUrl);

        return user;
    }

    private void fillFieldsForUser(User user, ManagedUserVM managedUserVM){
        user.setLogin(managedUserVM.getLogin());
        user.setFirstName(managedUserVM.getFirstName());
        user.setLastName(managedUserVM.getLastName());
        user.setEmail(managedUserVM.getEmail());
        user.setCity(managedUserVM.getCity());
        user.setStreet(managedUserVM.getStreet());
        user.setHouse(managedUserVM.getHouse());
        user.setFlat(managedUserVM.getFlat());
        user.setPassport(managedUserVM.getPassport());
        user.setBirthDate(managedUserVM.getBirthDate());
        log.debug("ATTENTION: from front {}; in back: {}", managedUserVM.getBirthDate(), user.getBirthDate());
        if (managedUserVM.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(managedUserVM.getLangKey());
        }
        if (managedUserVM.getAuthorities() != null) {
            Set<Authority> authorities = new HashSet<>();
            managedUserVM.getAuthorities().stream().forEach(
                authority -> authorities.add(authorityRepository.findOne(authority))
            );
            user.setAuthorities(authorities);
        }
    }

    public void updateUser(String firstName, String lastName, String email, String logo, String langKey) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(u -> {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
            u.setLogo(logo);
            u.setLangKey(langKey);
            userRepository.save(u);
            log.debug("Changed Information for User: {}", u);
        });
    }

    public void updateUser(ManagedUserVM managedUserVM){

        userRepository.findOneByLogin(managedUserVM.getLogin()).ifPresent(u -> {
            fillFieldsForUser(u, managedUserVM);
            userRepository.save(u);
            log.debug("Changed Information for User: {}", u);
        });
    }

    public void updateUser(Long id, String login, String firstName, String lastName, String email,
                           boolean activated, String langKey, Set<String> authorities) {

        userRepository
            .findOneById(id)
            .ifPresent(u -> {
                u.setLogin(login);
                u.setFirstName(firstName);
                u.setLastName(lastName);
                u.setEmail(email);
                u.setActivated(activated);
                u.setLangKey(langKey);
                Set<Authority> managedAuthorities = u.getAuthorities();
                managedAuthorities.clear();
                authorities.stream().forEach(
                    authority -> managedAuthorities.add(authorityRepository.findOne(authority))
                );
                log.debug("Changed Information for User: {}", u);
            });
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(u -> {
            userRepository.delete(u);
            log.debug("Deleted User: {}", u);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(u -> {
            String encryptedPassword = passwordEncoder.encode(password);
            u.setPassword(encryptedPassword);
            userRepository.save(u);
            log.debug("Changed password for User: {}", u);
        });
    }

    public void changeStatus(Long id) {
        User user = userRepository.findOne(id);
        if (user != null) {
            user.setActivated(!user.getActivated());
            userRepository.save(user);
            log.debug("Change status for User with login '{}'. Activated status is {}", user.getLogin(), user.getActivated());
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login).map(u -> {
            u.getAuthorities().size();
            return u;
        });
    }

    public List<ManagedUserVM> getDrivers() {
        Optional<User> user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        HashSet set = new HashSet();
        set.add(authorityRepository.getOne("ROLE_DRIVER"));
        List<ManagedUserVM> users = userRepository.
            findByCompanyAndAuthoritiesAndActivated(user.get().getCompany(), set, true)
            .stream()
            .map(ManagedUserVM::new)
            .collect(Collectors.toList());

        return users;
    }

    /*
            List<ManagedUserVM> managedUserVMs = page.getContent().stream()
                .map(ManagedUserVM::new)
                .collect(Collectors.toList());
     */
    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        User user = userRepository.findOne(id);
        user.getAuthorities().size(); // eagerly load the association
        return user;
    }


    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        Optional<User> optionalUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        User user = null;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.getAuthorities().size(); // eagerly load the association
        }
        return user;
    }


    public String uploadUserLogo(String file, String fileName, Long user_id, String rootDirectory) throws IOException, UploadException {
        User user = userRepository.findOne(user_id);
        if (user != null) {
            String imageName = UploadUtil.uploadImage(file, fileName, rootDirectory);
            if (user.getLogo() != null) {
                UploadUtil.deleteFile(rootDirectory + File.separator + user.getLogo());
            }
            /*user.setLogo(imageName);
            userRepository.save(user);*/
            return imageName;

        } else {
            return null;
        }
    }

    public void deleteUserLogo(Long user_id, String rootDirectory){
        User user = userRepository.findOne(user_id);
        if (user != null){
            if (user.getLogo() != null) {
                UploadUtil.deleteFile(rootDirectory + File.separator + user.getLogo());
            }
            user.setLogo(null);
            userRepository.save(user);
        }
    }



    public List<User> getUsersBelongCompanyWithoutAssignedTemplate(){

        Optional<User> optionalUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        if (!optionalUser.isPresent()) return Collections.emptyList();
        User admin = optionalUser.get();

        List<User> allEmployee = companyService.getCompanyUsersWithoutAdmin(admin);
        List<Template> templates = templateService.getTemplatesCreatedByCurrentAdmin();

        for (Template template : templates){
            if (allEmployee.contains(template.getRecipient())) {
                allEmployee.remove(template.getRecipient());
            }
        }

        return allEmployee;
    }

    public List<User> getUsersBelongCompanyWithoutAssignedTemplateIncludeUserById(Long id){

        Optional<User> optionalUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        if (!optionalUser.isPresent()) return Collections.emptyList();
        User admin = optionalUser.get();

        List<User> allEmployee = companyService.getCompanyUsersWithoutAdmin(admin);
        List<Template> templates = templateService.getTemplatesCreatedByCurrentAdmin();

        for (Template template : templates){
            if (allEmployee.contains(template.getRecipient()) && template.getRecipient().getId() != id) {
                allEmployee.remove(template.getRecipient());
            }
        }

        return allEmployee;
    }


    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p>
     * This is scheduled to get fired everyday, at midnight.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = LocalDate.now();
        persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1)).stream().forEach(token -> {
            log.debug("Deleting token {}", token.getSeries());
            User user = token.getUser();
            user.getPersistentTokens().remove(token);
            persistentTokenRepository.delete(token);
        });
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        ZonedDateTime now = ZonedDateTime.now();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }

    public List<ManagedUserVM> getFreeDrivers (Long from, Long to) {
        User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();

        ZonedDateTime dateFrom = ZonedDateTime.ofInstant(new Date(from).toInstant(), ZoneId.systemDefault());
        ZonedDateTime dateTo = ZonedDateTime.ofInstant(new Date(to).toInstant(), ZoneId.systemDefault());

        HashSet set = new HashSet();
        set.add(authorityRepository.getOne("ROLE_DRIVER"));
        List<User> drivers = userRepository.findByCompanyAndAuthoritiesAndActivated(user.getCompany(),set, true);

        Set<User> busyDriversSet = routeListRepository.findRouteListsByDate(user.getCompany(),dateFrom,dateTo)
            .stream()
            .map(routeList -> {
                return routeList.getWaybill().getDriver();
            }).collect(Collectors.toSet());

        drivers.removeAll(busyDriversSet);

        return drivers.stream().map(ManagedUserVM::new).collect(Collectors.toList());
    }

}
