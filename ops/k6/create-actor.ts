import http from "k6/http";
import { getAccessToken } from "./helper/token.ts";
import { paths } from "./helper/url.ts";
import { check } from "k6";

export default function () {
    const authData = getAccessToken();

    if (!authData?.accessToken) {
        console.error('No access token provided');
        return;
    }

    const headers = {
        'Authorization': `Bearer ${authData.accessToken}`,
        'Content-Type': 'application/json',
    };

    const timestamp = Date.now();
    const body = JSON.stringify({
        "surname": "Pitanga" + " " + timestamp,
        "name": "Camila",
        "nationalityCode": "BR",
        "birthDate": "1977-06-14"
    });

    const postRes = http.post(paths.actors, body, { headers: headers });
    check(postRes, {
        'POST actors status is 201': (r) => (r.status === 201),
    });

    if (postRes.status !== 201) {
        console.error(`Failed to create actor: ${postRes.body}`);
    }

}