package com.azen.user.controller;

import com.azen.user.bean.Car;
import com.azen.user.service.CarService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/car")
public class CarController {
    private final CarService carService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("addCar")
    public Boolean addCar(Car car, HttpServletRequest request) {
        car.setUId((Integer) request.getSession().getAttribute("userId"));
        if (carService.addCarCount(car) == 0) {
            return carService.addCar(car) > 0;
        } else {
            Car car1 = carService.carId(car);
            car1.setCNum(car1.getCNum() + car.getCNum());
            return carService.updateCar(car1) > 0;
        }
    }

    @PostMapping("updateCar")
    public Boolean updateCar(Car car) {
        return carService.updateCar(car) > 0;
    }

    @PostMapping("carPageInfo")
    public PageInfo<Car> carPageInfo(@RequestParam(defaultValue = "1", name = "pageNumber") Integer pageNumber, HttpServletRequest request) {
        PageHelper.startPage(pageNumber, 5);
        return new PageInfo<>(carService.carForUid((Integer) request.getSession().getAttribute("userId")));
    }

    @PostMapping("deleteCarForCId")
    public Boolean deleteCarForCId(Integer id) {
        return carService.deleteCarForCId(id) > 0;
    }

    @GetMapping("sendMsg/{message}")
    public void sendMsg(@PathVariable String message) {
        log.info("当前时间：{},发送一条信息给两个 TTL 队列:{}", new Date(), message);
        rabbitTemplate.convertAndSend("X", "XA", "消息来自 ttl 为 10S 的队列: " + message);
        rabbitTemplate.convertAndSend("X", "XB", "消息来自 ttl 为 40S 的队列: " + message);
    }

}
