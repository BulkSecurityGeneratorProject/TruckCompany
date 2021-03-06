(function () {
    'use strict';

    angular
        .module('truckCompanyApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('companyowner.waybills', {
                parent: 'companyowner',
                url: '/companyowner/waybills',

                data: {
                    authorities: ['ROLE_COMPANYOWNER'],
                    pageTitle: 'activate.title'
                },
                views: {
                    'page@roles': {
                        templateUrl: 'app/roles/companyowner/waybills/companyowner.waybills.html',
                        controller: 'CompanyownerWaybillsController',
                        controllerAs: 'vm'
                    }
                },
                params:{
                    page: 1,
                    size: 5
                },
                resolve: {
                    pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                        return {
                            page: PaginationUtil.parsePage($stateParams.page),
                            size: PaginationUtil.parsePage($stateParams.size)
                        };
                    }],
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('companyowner');
                        return $translate.refresh();
                    }]
                }
            });
    }
})();
