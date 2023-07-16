package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param request HttpServletRequest
     * @param employee Employee
     * @return Result
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1、将密码进行MD5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2、根据用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3、没有查到返回失败结果
        if (emp == null) {
            return Result.error("登录失败");
        }

        // 4、密码比对，不一致返回失败结果
        if (!emp.getPassword().equals(password)) {
            return Result.error("登录失败");
        }

        // 5、判断员工状态
        if (emp.getStatus() == 0) {
            return Result.error("账号已禁用");
        }

        // 6、登录成功，将id存入session
        request.getSession().setAttribute("employee", emp.getId());
        return Result.success(emp);
    }

    /**
     * 退出
     * @param request HttpServletRequest
     * @return Result
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 清除Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }

    /**
     * 新增员工
     * @param request HttpServletRequest
     * @param employee Employee
     * @return Result
     */
    @PostMapping
    public Result<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        employeeService.save(employee);

        return Result.success("新增员工成功");
    }

    /**
     * 列表数据
     * @param page Integer
     * @param pageSize Integer
     * @param name String
     * @return Result
     */
    @GetMapping("/page")
    public Result<Page<Employee>> page(Integer page, Integer pageSize, String name) {
        // 构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo, queryWrapper);

        return Result.success(pageInfo);
    }


    /**
     * 修改员工
     * @param request HttpServletRequest
     * @param employee Employee
     * @return Result
     */
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        employeeService.updateById(employee);
        return Result.success("员工信息修改成功");
    }

    /**
     * 通过id查找员工
     * @param id Long
     * @return Result
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("没有查到员工信息");
    }

}
