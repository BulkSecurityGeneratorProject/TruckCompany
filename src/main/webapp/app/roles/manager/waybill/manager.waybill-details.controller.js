/**
 * Created by Dmitry on 17.11.2016.
 */
(function () {
    'use strict';

    angular
        .module('truckCompanyApp')
        .controller('ManagerWaybillDetailsController', ManagerWaybillDetailsController);

    ManagerWaybillDetailsController.$inject = ['Principal', 'entity', '$uibModalInstance', 'Waybill', '$http', '$scope'];

    function ManagerWaybillDetailsController(Principal, entity, $uibModalInstance, Waybill, $http, $scope) {
        var vm = this;

        vm.clear = clear;
        vm.waybill = entity;
        vm.changeWaybillState = changeWaybillState;
        getAccount();

        console.log(vm.waybill);
        function getAccount() {
            Principal.identity().then(function (account) {
                vm.account = account;
                vm.fullName = account.firstName + ' ' + account.lastName;
            });
        }

        function changeWaybillState(id) {
            vm.waybill.manager = vm.account;
            vm.waybill.routeList = null;
            vm.waybill.writeOffAct = null;
            if (id == 1) {
                vm.waybill.state = 'CHECKED';
            } else {
                vm.waybill.state = 'REJECTED';
            }
            angular.forEach(vm.waybill.goods, function (value) {
                value.state = 'ACCEPTED';
            });
            $http.put('/api/goods', vm.waybill.goods);
            Waybill.update(vm.waybill);
            console.log(vm.waybill.goods);
            $scope.$emit('waybillState', vm.waybill);
        }

        function clear() {

            $uibModalInstance.close();
        }

        vm.dateCheckedPicker = {
            date: new Date(),
            datepickerOptions : {
                maxDate: null,
                showWeeks: false
            }
        };

        this.openCalendar = function(e, picker) {
            vm[picker].open = true;
        };
    }
})();
