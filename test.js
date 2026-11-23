import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
   vus: 200,
  duration: '10s',
};

export default function () {
    const url = 'http://localhost:9091/budgets/recommend?totalAmount=2000000';

  const params = {
     headers: {
         // 로그인 후 발급받은 토큰
       'Authorization': 'Bearer {AccessToken}',
      },
   };

   const res = http.get(url, params);

  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(0.5);
}