const API_BASE_URL = 'https://api.minimall.com';
const APP_NAME = 'Minimall';

const ORDER_STATUS = {
  ALL: 0,
  PENDING_PAYMENT: 1,
  PENDING_SHIPMENT: 2,
  PENDING_RECEIPT: 3,
  COMPLETED: 4,
};

module.exports = { API_BASE_URL, APP_NAME, ORDER_STATUS };
