/**
 * Created by Vladimir on 30.10.2016.
 */
(function () {
    'use strict';

    angular
        .module('truckCompanyApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
            .state('admincompany.templates', {
                parent: 'admincompany',
                url: '/admincompany/templates/page&size',

                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'activate.title',
                },
                views: {
                    'page@roles': {
                        templateUrl: 'app/roles/admincompany/templates/admincompany.templates.html',
                        controller: 'AdmincompanyTemplatesController',
                        controllerAs: 'vm'
                    },
                },
                params: {
                    page: 1,
                    size: 10
                },
                resolve: {
                    pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                        return {
                            page: PaginationUtil.parsePage($stateParams.page),
                            size: PaginationUtil.parsePage($stateParams.size)
                            /*sort: $stateParams.sort,
                             predicate: PaginationUtil.parsePredicate($stateParams.sort),
                             ascending: PaginationUtil.parseAscending($stateParams.sort)*/
                        };
                    }],
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('admincompany');
                        return $translate.refresh();
                    }]
                }
            })
            .state('admincompany.template', {
                parent: 'admincompany',
                url: '/admincompany/templates/:id',

                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'activate.title'
                },
                views: {
                    'page@roles': {
                        templateUrl: 'app/roles/admincompany/templates/admincompany.template.html',
                        controller: 'AdmincompanyTemplateController',
                        controllerAs: 'vm'
                    },
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('admincompany');
                        return $translate.refresh();
                    }]
                }
            })
            .state('admincompany.createtemplate', {
                parent: 'admincompany',
                url: '/admincompany/templatecreate',

                data: {
                    authorities: ['ROLE_ADMIN'],
                    pageTitle: 'activate.title'
                },
                views: {
                    'page@roles': {
                        templateUrl: 'app/roles/admincompany/templates/admincompany.templatecreate.html',
                        controller: 'AdmincompanyTemplateCreateController',
                        controllerAs: 'vm'
                    },
                },
                resolve: {
                    translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                        $translatePartialLoader.addPart('admincompany');
                        return $translate.refresh();
                    }]
                }
            });
    }
})();
